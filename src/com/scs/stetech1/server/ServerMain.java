package com.scs.stetech1.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.math.Vector3f;
import com.jme3.network.ConnectionListener;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeContext;
import com.jme3.system.JmeContext.Type;
import com.scs.stetech1.components.ICalcHitInPast;
import com.scs.stetech1.components.ICollideable;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.components.IProcessByServer;
import com.scs.stetech1.netmessages.EntityUpdateMessage;
import com.scs.stetech1.netmessages.GameSuccessfullyJoinedMessage;
import com.scs.stetech1.netmessages.GeneralCommandMessage;
import com.scs.stetech1.netmessages.MyAbstractMessage;
import com.scs.stetech1.netmessages.NewEntityMessage;
import com.scs.stetech1.netmessages.NewPlayerRequestMessage;
import com.scs.stetech1.netmessages.PingMessage;
import com.scs.stetech1.netmessages.PlayerInputMessage;
import com.scs.stetech1.netmessages.PlayerLeftMessage;
import com.scs.stetech1.netmessages.RemoveEntityMessage;
import com.scs.stetech1.netmessages.UnknownEntityMessage;
import com.scs.stetech1.netmessages.WelcomeClientMessage;
import com.scs.stetech1.server.entities.ServerPlayersAvatar;
import com.scs.stetech1.shared.AbstractPlayersAvatar;
import com.scs.stetech1.shared.EntityTypes;
import com.scs.stetech1.shared.IEntityController;
import com.scs.stetech1.shared.entities.Crate;
import com.scs.stetech1.shared.entities.DebuggingSphere;
import com.scs.stetech1.shared.entities.Floor;
import com.scs.stetech1.shared.entities.PhysicalEntity;
import com.scs.stetech1.shared.entities.Wall;

import ssmith.swing.LogWindow;
import ssmith.util.FixedLoopTime;
import ssmith.util.RealtimeInterval;

public class ServerMain extends SimpleApplication implements IEntityController, ConnectionListener, MessageListener<HostedConnection>, PhysicsCollisionListener  {

	private static final String PROPS_FILE = Settings.NAME.replaceAll(" ", "") + "_settings.txt";

	private static AtomicInteger nextEntityID = new AtomicInteger();

	private Server myServer;
	private HashMap<Integer, ClientData> clients = new HashMap<>(10); // PlayerID::ClientData
	public HashMap<Integer, IEntity> entities = new HashMap<>(100); // EntityID::Entity

	public static GameProperties properties;
	private FixedLoopTime loopTimer = new FixedLoopTime(Settings.SERVER_TICKRATE_MS);
	private RealtimeInterval sendPingInt = new RealtimeInterval(Settings.PING_INTERVAL_MS);
	private RealtimeInterval sendEntityUpdatesInt = new RealtimeInterval(Settings.SERVER_SEND_UPDATE_INTERVAL_MS);
	public BulletAppState bulletAppState;
	private List<MyAbstractMessage> unprocessedMessages = new LinkedList<>();
	private ExecutorService executor = Executors.newFixedThreadPool(20);
	private LogWindow logWindow;
	private IConsole console;

	public static void main(String[] args) {
		try {
			ServerMain app = new ServerMain();
			app.setPauseOnLostFocus(false);
			if (Settings.HEADLESS_SERVER) {
				app.start(JmeContext.Type.Headless);
			} else {
				app.start();				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public ServerMain() throws IOException {
		properties = new GameProperties(PROPS_FILE);
		logWindow = new LogWindow("Server", 400, 300);
		console = new ServerConsole(this);
	}


	@Override
	public void simpleInitApp() {
		try {
			myServer = Network.createServer(Settings.PORT);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		Settings.registerMessages();

		myServer.start();
		myServer.addConnectionListener(this);

		myServer.addMessageListener(this, PingMessage.class);
		myServer.addMessageListener(this, NewPlayerRequestMessage.class);
		myServer.addMessageListener(this, GameSuccessfullyJoinedMessage.class);
		myServer.addMessageListener(this, PlayerInputMessage.class);
		myServer.addMessageListener(this, UnknownEntityMessage.class);
		myServer.addMessageListener(this, NewEntityMessage.class);
		myServer.addMessageListener(this, EntityUpdateMessage.class);
		myServer.addMessageListener(this, PlayerLeftMessage.class);

		// Set up Physics
		bulletAppState = new BulletAppState();
		getStateManager().attach(bulletAppState);
		bulletAppState.getPhysicsSpace().addCollisionListener(this);
		//bulletAppState.getPhysicsSpace().addTickListener(this);
		createGame();
		loopTimer.start();
	}


	@Override
	public void simpleUpdate(float tpf_secs) { //this.rootNode.getChild(2).getWorldTranslation();
		StringBuilder strDebug = new StringBuilder();

		if (myServer.hasConnections()) { // this.rootNode

			// Process all messsages
			synchronized (unprocessedMessages) {
				while (!this.unprocessedMessages.isEmpty()) {
					MyAbstractMessage message = this.unprocessedMessages.remove(0);
					ClientData client = message.client;
					if (message instanceof NewPlayerRequestMessage) {
						NewPlayerRequestMessage newPlayerMessage = (NewPlayerRequestMessage) message;
						client.playerName = newPlayerMessage.name;
						client.avatar = createPlayersAvatar(client);
						// Send newplayerconf message
						broadcast(client.conn, new GameSuccessfullyJoinedMessage(client.getPlayerID(), client.avatar.id));
						sendEntityListToClient(client);
						client.clientStatus = ClientData.Status.InGame;
						
						// Send them a ping to get ping time
						broadcast(client.conn, new PingMessage(true));

					} else if (message instanceof UnknownEntityMessage) {
						UnknownEntityMessage uem = (UnknownEntityMessage) message;
						IEntity e = this.entities.get(uem.entityID);
						this.sendNewEntity(client, e);

					} else if (message instanceof PlayerLeftMessage) {
						this.connectionRemoved(this.myServer, client.conn);

					} else if (message instanceof PlayerInputMessage) { // Process these here so the inputs don't change values mid-thread
						PlayerInputMessage pim = (PlayerInputMessage)message;
						if (pim.timestamp > client.latestInputTimestamp) {
							client.remoteInput.decodeMessage(pim);
							client.latestInputTimestamp = pim.timestamp;
						}

					} else {
						throw new RuntimeException("Unknown message type: " + message);
					}
				}
			}

			if (sendPingInt.hitInterval()) {
				broadcast(new PingMessage(true));
			}

			synchronized (this.clients) {
				// If any avatars are shooting a gun the requires "rewinding time", rewind all avatars and calc the hits all together to save time
				boolean areAnyPlayersShooting = false;
				for (ClientData c : this.clients.values()) {
					ServerPlayersAvatar avatar = c.avatar;
					if (avatar != null && avatar.isShooting() && avatar.abilityGun instanceof ICalcHitInPast) {
						areAnyPlayersShooting = true;
						break;
					}
				}
				if (areAnyPlayersShooting) {
					this.rewindAllAvatars(System.currentTimeMillis() - Settings.CLIENT_RENDER_DELAY); // todo - is time correct?
					this.rootNode.updateGeometricState();
					for (ClientData c : this.clients.values()) {
						ServerPlayersAvatar avatar = c.avatar;
						if (avatar != null && avatar.isShooting() && avatar.abilityGun instanceof ICalcHitInPast) {
							ICalcHitInPast chip = (ICalcHitInPast) avatar.abilityGun;
							chip.setTarget(avatar.calcHitEntity(avatar.getShootDir(), chip.getRange())); // This damage etc.. is calculated later
						}
					}
					this.restoreAllAvatarPositions();
				}
			}

			boolean sendUpdates = sendEntityUpdatesInt.hitInterval();
			synchronized (entities) {
				// Loop through the entities
				for (IEntity e : entities.values()) {
					if (e instanceof IProcessByServer) {
						IProcessByServer p = (IProcessByServer)e;
						p.process(this, tpf_secs);
					}

					if (e instanceof PhysicalEntity) {
						PhysicalEntity sc = (PhysicalEntity)e;
						strDebug.append(sc.name + " Pos: " + sc.getWorldTranslation() + "\n");
						if (sc.type == EntityTypes.AVATAR) {
							AbstractPlayersAvatar av = (AbstractPlayersAvatar)sc;
							strDebug.append("WalkDir: " + av.playerControl.getWalkDirection() + "   Velocity: " + av.playerControl.getVelocity().length() + "\n");
						}
						if (sendUpdates) {
							if (sc.hasMoved()) { // Don't send if not moved (unless Avatar)
								broadcast(new EntityUpdateMessage(sc, false));
								//Settings.p("Sending EntityUpdateMessage for " + sc);
							}
						}
					}
				}
			}

			// Loop through clients
			/*synchronized (clients) {
				for (ClientData client : clients.values()) {
					client.sendMessages(this.myServer);
				}
			}*/
		}

		this.logWindow.setText(strDebug.toString());

		loopTimer.waitForFinish(); // Keep clients and server running at same speed
		loopTimer.start();
	}


	@Override
	public void messageReceived(HostedConnection source, Message message) {
		ClientData client = null;
		synchronized (clients) {
			client = clients.get(source.getId());
		}

		if (client == null) {
			return;
		}
		//Settings.p("Rcvd " + message.getClass().getSimpleName());

		MyAbstractMessage msg = (MyAbstractMessage)message;

		if (message instanceof PingMessage) {
			PingMessage pingMessage = (PingMessage) message;
			if (pingMessage.s2c) {
				try {
					long rttDuration = System.currentTimeMillis() - pingMessage.originalSentTime;
					client.pingRTT = client.pingCalc.add(rttDuration);
					client.serverToClientDiffTime = pingMessage.responseSentTime - pingMessage.originalSentTime - (client.pingRTT/2); // If running on the same server, this should be 0! (or close enough)
					//Settings.p("Client rtt = " + client.pingRTT);
					//Settings.p("serverToClientDiffTime = " + client.serverToClientDiffTime);
				} catch (NullPointerException npe) {
					npe.printStackTrace();
				}
			} else {
				// Send it back to the client
				pingMessage.responseSentTime = System.currentTimeMillis();
				broadcast(client.conn, pingMessage);
			}

		} else {
			msg.client = client;
			synchronized (this.unprocessedMessages) {
				this.unprocessedMessages.add(msg);
			}
		}

	}


	private ServerPlayersAvatar createPlayersAvatar(ClientData client) {
		int id = getNextEntityID();
		ServerPlayersAvatar avatar = new ServerPlayersAvatar(this, client.getPlayerID(), client.remoteInput, id);
		avatar.moveToStartPostion(true);
		this.addEntity(avatar);
		return avatar;
	}


	private void sendEntityListToClient(ClientData client) {
		synchronized (entities) {
			for (IEntity e : entities.values()) {
				this.sendNewEntity(client, e);
			}
			GeneralCommandMessage aes = new GeneralCommandMessage();
			broadcast(client.conn, aes);
		}
	}


	private void sendNewEntity(ClientData client, IEntity e) {
		if (e instanceof PhysicalEntity) {
			PhysicalEntity se = (PhysicalEntity)e;
			NewEntityMessage nem = new NewEntityMessage(se);
			nem.force = true;
			broadcast(client.conn, nem);
		}
	}


	/*@Override
	public void handleError(Object obj, Throwable ex) {
		Settings.p("Network error with " + obj + ": " + ex);
		ex.printStackTrace();

		// Remove connection
		this.connectionRemoved(this.myServer, (HostedConnection)obj);

	}*/


	@Override
	public void connectionAdded(Server arg0, HostedConnection conn) {
		Settings.p("Client connected!");
		synchronized (clients) {
			clients.put(conn.getId(), new ClientData(conn, this.getCamera(), this.getInputManager()));
		}
		broadcast(conn, new WelcomeClientMessage());

	}


	@Override
	public void connectionRemoved(Server arg0, HostedConnection source) {
		Settings.p("connectionRemoved()");
		synchronized (clients) {
			ClientData client = clients.get(source.getId());
			if (client != null) { // For some reason, connectionRemoved() gets called multiple times
				this.playerLeft(client);
			}
		}
	}


	private void playerLeft(ClientData client) {
		try {
			Settings.p("Removing player " + client.getPlayerID());
			synchronized (clients) {
				this.clients.remove(client.getPlayerID());
			}
			// Remove avatar
			if (client.avatar != null) {
				this.removeEntity(client.avatar.id);
			}
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
	}


	@Override
	public void addEntity(IEntity e) {
		synchronized (entities) {
			//Settings.p("Trying to add " + e + " (id " + e.getID() + ")");
			if (this.entities.containsKey(e.getID())) {
				throw new RuntimeException("Entity id " + e.getID() + " already exists: " + e);
			}
			this.entities.put(e.getID(), e);

			// Tell clients
			if (e instanceof PhysicalEntity) {
				PhysicalEntity se = (PhysicalEntity)e;
				NewEntityMessage nem = new NewEntityMessage(se);
				nem.force = true;
				broadcast(nem);
			}
		}

	}


	@Override
	public void removeEntity(int id) {
		Settings.p("Removing entity " + id);
		try {
			synchronized (entities) {
				this.entities.remove(id);
			}
			this.broadcast(new RemoveEntityMessage(id));
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
	}


	public BulletAppState getBulletAppState() {
		return bulletAppState;
	}



	private void createGame() { // todo - make abstract
		new Floor(this, getNextEntityID(), 0, 0, 0, 30, .5f, 30, "Textures/floor015.png", null);
		//new DebuggingSphere(this, getNextEntityID(), 0, 0, 0);
		//new Crate(this, getNextEntityID(), 8, 2, 8, 1, 1, 1f, "Textures/crate.png", 45);
		//new Crate(this, getNextEntityID(), 8, 5, 8, 1, 1, 1f, "Textures/crate.png", 65);
		new Wall(this, getNextEntityID(), 0, 0, 0, 10, 10, "Textures/seamless_bricks/bricks2.png", 0);
	}


	@Override
	public void collision(PhysicsCollisionEvent event) {
		String s = event.getObjectA().getUserObject().toString() + " collided with " + event.getObjectB().getUserObject().toString();
		
		if (event.getObjectB().getUserObject() instanceof Floor == false) {
			System.out.println(s);
		}

		PhysicalEntity a=null, b=null;
		Object oa = event.getObjectA().getUserObject(); 
		if (oa instanceof Spatial) {
			Spatial ga = (Spatial)event.getObjectA().getUserObject(); 
			a = ga.getUserData(Settings.ENTITY);
		} else if (oa instanceof PhysicalEntity) {
			a = (PhysicalEntity)oa;
		}

		Object ob = event.getObjectB().getUserObject(); 
		if (ob instanceof Spatial) {
			Spatial gb = (Spatial)event.getObjectB().getUserObject(); 
			b = gb.getUserData(Settings.ENTITY);
		} else if (oa instanceof PhysicalEntity) {
			b = (PhysicalEntity)ob;
		}

		if (a != null && b != null) {
			if (a instanceof ICollideable && b instanceof ICollideable) {
				//Settings.p(a + " has collided with " + b);
				ICollideable ica = (ICollideable)a;
				ICollideable icb = (ICollideable)b;
				ica.collidedWith(icb);
				icb.collidedWith(ica);
			}
		} else {
			if (a == null) {
				Settings.p(oa + " has no entity data!");
			}
			if (b == null) {
				Settings.p(ob + " has no entity data!");
			}
		}
	}


	@Override
	public boolean isServer() {
		return true;
	}


	public static int getNextEntityID() {
		return nextEntityID.getAndAdd(1);
	}


	public void broadcast(final MyAbstractMessage msg) {
		if (Settings.ARTIFICIAL_COMMS_DELAY == 0) {
			myServer.broadcast(msg);
		}
		else {
			Thread t = new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(Settings.ARTIFICIAL_COMMS_DELAY);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					myServer.broadcast(msg);
				}
			};
			t.start();
		}
	}


	private void broadcast(final HostedConnection conn, final MyAbstractMessage msg) {
		if (Settings.ARTIFICIAL_COMMS_DELAY == 0) {
			myServer.broadcast(Filters.equalTo(conn), msg);
		}
		else {
			Runnable t = new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(Settings.ARTIFICIAL_COMMS_DELAY);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					myServer.broadcast(Filters.equalTo(conn), msg);
				}
			};
			executor.execute(t);

		}
	}


	private void rewindAllAvatars(long time) {
		synchronized (this.clients) {
			for (ClientData c : this.clients.values()) {
				c.avatar.rewindPosition(time);
			}
		}
	}


	private void restoreAllAvatarPositions() {
		synchronized (this.clients) {
			for (ClientData c : this.clients.values()) {
				c.avatar.restorePosition();
			}
		}
	}


	public void handleCommand(String cmd) {
		if (cmd.equals("warp")) {
			for(ClientData client : this.clients.values()) {
				if (client.avatar != null) {
					Settings.p("Warping player");
					client.avatar.playerControl.warp(new Vector3f(10, 10, 10));
					break;
				}
			}
		} else if (cmd.equals("quit")) {
			this.myServer.close();
			this.stop();
		}
	}


	@Override
	public Type getJmeContext() {
		return getContext().getType();
	}

}
