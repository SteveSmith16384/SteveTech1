package com.scs.stetech1.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import ssmith.util.FixedLoopTime;
import ssmith.util.RealtimeInterval;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.network.ConnectionListener;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.system.JmeContext;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.components.IProcessable;
import com.scs.stetech1.netmessages.AllEntitiesSentMessage;
import com.scs.stetech1.netmessages.EntityUpdateMessage;
import com.scs.stetech1.netmessages.MyAbstractMessage;
import com.scs.stetech1.netmessages.NewEntityMessage;
import com.scs.stetech1.netmessages.NewPlayerAckMessage;
import com.scs.stetech1.netmessages.NewPlayerRequestMessage;
import com.scs.stetech1.netmessages.PingMessage;
import com.scs.stetech1.netmessages.PlayerInputMessage;
import com.scs.stetech1.netmessages.PlayerLeftMessage;
import com.scs.stetech1.netmessages.RemoveEntityMessage;
import com.scs.stetech1.netmessages.UnknownEntityMessage;
import com.scs.stetech1.server.entities.ServerPlayersAvatar;
import com.scs.stetech1.shared.IEntityController;
import com.scs.stetech1.shared.entities.Floor;
import com.scs.stetech1.shared.entities.PhysicalEntity;

public class ServerMain extends SimpleApplication implements IEntityController, ConnectionListener, MessageListener<HostedConnection>, PhysicsCollisionListener  {

	private static final String PROPS_FILE = Settings.NAME.replaceAll(" ", "") + "_settings.txt";

	private static AtomicInteger nextEntityID = new AtomicInteger();

	private Server myServer;
	private HashMap<Integer, ClientData> clients = new HashMap<>(10); // PlayerID::ClientData
	public HashMap<Integer, IEntity> entities = new HashMap<>(100); // EntityID::Entity

	public static SorcerersProperties properties;
	private FixedLoopTime loopTimer = new FixedLoopTime(Settings.SERVER_TICKRATE_MS);
	private RealtimeInterval sendPingInt = new RealtimeInterval(Settings.PING_INTERVAL_MS);
	private RealtimeInterval sendEntityUpdatesInt = new RealtimeInterval(Settings.SERVER_SEND_UPDATE_INTERVAL_MS);
	public BulletAppState bulletAppState;
	private List<MyAbstractMessage> unprocessedMessages = new LinkedList<>();

	public static void main(String[] args) {
		try {
			ServerMain app;
			app = new ServerMain();
			app.setPauseOnLostFocus(false);
			app.start(JmeContext.Type.Headless);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public ServerMain() throws IOException {
		properties = new SorcerersProperties(PROPS_FILE);

		Settings.Register();

		myServer = Network.createServer(Settings.PORT);
		myServer.start();
		myServer.addConnectionListener(this);

		myServer.addMessageListener(this, PingMessage.class);
		myServer.addMessageListener(this, NewPlayerRequestMessage.class);
		myServer.addMessageListener(this, NewPlayerAckMessage.class);
		myServer.addMessageListener(this, PlayerInputMessage.class);
		myServer.addMessageListener(this, UnknownEntityMessage.class);
		myServer.addMessageListener(this, NewEntityMessage.class);
		myServer.addMessageListener(this, EntityUpdateMessage.class);
		myServer.addMessageListener(this, PlayerLeftMessage.class);

	}


	@Override
	public void simpleInitApp() {
		// Set up Physics
		bulletAppState = new BulletAppState();
		getStateManager().attach(bulletAppState);
		bulletAppState.getPhysicsSpace().addCollisionListener(this);
		//bulletAppState.getPhysicsSpace().addTickListener(this);
		//bulletAppState.getPhysicsSpace().setAccuracy(1f / 80f);

		createGame();
		loopTimer.start();
	}


	@Override
	public void simpleUpdate(float tpf_secs) {
		synchronized (unprocessedMessages) {
			while (!this.unprocessedMessages.isEmpty()) {
				MyAbstractMessage message = this.unprocessedMessages.remove(0);
				ClientData client = message.client;
				if (message instanceof NewPlayerRequestMessage) {
					NewPlayerRequestMessage newPlayerMessage = (NewPlayerRequestMessage) message;
					client.playerName = newPlayerMessage.name;
					client.avatarID = createPlayersAvatar(client);
					// Send newplayerconf message
					broadcast(client.conn, new NewPlayerAckMessage(client.getPlayerID(), client.avatarID));
					sendEntityListToClient(client);

					// Send them a ping to get ping time
					broadcast(client.conn, new PingMessage(true));

				} else if (message instanceof UnknownEntityMessage) {
					UnknownEntityMessage uem = (UnknownEntityMessage) message;
					IEntity e = this.entities.get(uem.entityID);
					this.sendNewEntity(client, e);

				} else if (message instanceof PlayerLeftMessage) {
					this.connectionRemoved(this.myServer, client.conn);

				} else {
					throw new RuntimeException("Unknown message type: " + message);
				}
			}
		}

		if (myServer.hasConnections()) { // this.rootNode
			if (sendPingInt.hitInterval()) {
				broadcast(new PingMessage(true));
			}

			boolean sendUpdates = sendEntityUpdatesInt.hitInterval();
			synchronized (entities) {
				// Loop through the entities
				for (IEntity e : entities.values()) {
					if (e instanceof IProcessable) {
						IProcessable p = (IProcessable)e;
						p.process(tpf_secs);
					}

					if (sendUpdates) {
						if (e instanceof PhysicalEntity) {
							PhysicalEntity sc = (PhysicalEntity)e;
							if (sc.hasMoved()) { // Don't send if not moved
								/*if (sc.type == EntityTypes.AVATAR) {
									Settings.p("Sending avatar pos:" + sc.getWorldTranslation());
								}*/
								broadcast(new EntityUpdateMessage(sc));
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

		loopTimer.waitForFinish(); // Keep clients and server running at same speed
		loopTimer.start();
	}


	@Override
	public void messageReceived(HostedConnection source, Message message) {
		ClientData client = null;
		synchronized (clients) {
			client = clients.get(source.getId());
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
					/*
					 * Server sent time: 2000
					 * Client response time: 1000
					 * Ping: 200 
					 * clientToServerDiffTime: 1000 - 2000 + (200/2) = -900 
					 */
					Settings.p("Client rtt = " + client.pingRTT);
					Settings.p("serverToClientDiffTime = " + client.serverToClientDiffTime);
				} catch (NullPointerException npe) {
					npe.printStackTrace();
				}
			} else {
				// Send it back to the client
				pingMessage.responseSentTime = System.currentTimeMillis();
				broadcast(client.conn, pingMessage);
			}

		} else if (message instanceof PlayerInputMessage) {
			PlayerInputMessage pim = (PlayerInputMessage)message;
			if (pim.timestamp > client.latestInputTimestamp) {
				client.remoteInput.decodeMessage(pim);
				client.latestInputTimestamp = pim.timestamp;
			}

		} else {
			msg.client = client;
			synchronized (this.unprocessedMessages) {
				this.unprocessedMessages.add(msg);
			}
		}

	}


	private int createPlayersAvatar(ClientData client) {
		int id = getNextEntityID();
		ServerPlayersAvatar avatar = new ServerPlayersAvatar(this, client.getPlayerID(), client.remoteInput, id);
		avatar.moveToStartPostion(true);
		this.addEntity(avatar);
		return avatar.id;
	}


	private void sendEntityListToClient(ClientData client) {
		synchronized (entities) {
			for (IEntity e : entities.values()) {
				this.sendNewEntity(client, e);
			}
			AllEntitiesSentMessage aes = new AllEntitiesSentMessage();
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
			clients.put(conn.getId(), new ClientData(conn));
		}

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
			if (client.avatarID >= 0) {
				this.removeEntity(client.avatarID);
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



	private void createGame() {
		int id = getNextEntityID();
		new Floor(this, id, 0, 0, 0, 30, .5f, 30, "Textures/floor015.png", null);
		id = getNextEntityID();
		//todo - re-add new Crate(this, id, 8, 2, 8, 1, 1, 1f, "Textures/crate.png", 45);
	}


	@Override
	public void collision(PhysicsCollisionEvent event) {
		//String s = event.getObjectA().getUserObject().toString() + " collided with " + event.getObjectB().getUserObject().toString();
		//System.out.println(s);
		/*if (s.equals("Entity:Player collided with cannon ball (Geometry)")) {
			int f = 3;
		}*/

		//String s = event.getObjectA().getUserObject().toString() + " collided with " + event.getObjectB().getUserObject().toString();
		//System.out.println(s);
		/*if (s.equals("Entity:Player collided with cannon ball (Geometry)")) {
			int f = 3;
		}*/

		/*todo PhysicalEntity a=null, b=null;
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
			//CollisionLogic.collision(this, a, b);
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
		}*/
	}


	@Override
	public boolean isServer() {
		return true;
	}


	private static int getNextEntityID() {
		return nextEntityID.getAndAdd(1);
	}


	private void broadcast(final MyAbstractMessage msg) {
		if (Settings.COMMS_DELAY == 0) {
			myServer.broadcast(msg);
		}
		else {
			Thread t = new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(Settings.COMMS_DELAY);
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
		if (Settings.COMMS_DELAY == 0) {
			myServer.broadcast(Filters.equalTo(conn), msg);
		}
		else {
			Thread t = new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(Settings.COMMS_DELAY);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					myServer.broadcast(Filters.equalTo(conn), msg);
				}
			};
			t.start();
		}
	}
}
