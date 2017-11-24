package com.scs.stetech1.server;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeContext.Type;
import com.scs.simplephysics.ISimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stetech1.components.ICalcHitInPast;
import com.scs.stetech1.components.ICollideable;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.components.IProcessByServer;
import com.scs.stetech1.entities.PhysicalEntity;
import com.scs.stetech1.entities.ServerPlayersAvatar;
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
import com.scs.stetech1.networking.IMessageServer;
import com.scs.stetech1.networking.IMessageServerListener;
import com.scs.stetech1.networking.KryonetServer;
import com.scs.stetech1.shared.IEntityController;
import com.scs.testgame.entities.Floor;
import com.scs.testgame.entities.TestGameServerPlayersAvatar;

import ssmith.swing.LogWindow;
import ssmith.util.FixedLoopTime;
import ssmith.util.RealtimeInterval;

public abstract class AbstractGameServer extends SimpleApplication implements IEntityController, PhysicsCollisionListener, IMessageServerListener, ISimplePhysicsController  {

	private static final String PROPS_FILE = Settings.NAME.replaceAll(" ", "") + "_settings.txt";

	private static AtomicInteger nextEntityID = new AtomicInteger();

	public IMessageServer networkServer;
	private HashMap<Integer, ClientData> clients = new HashMap<>(10); // PlayerID::ClientData
	private HashMap<Integer, IEntity> entities = new HashMap<>(100); // EntityID::Entity

	public static GameProperties properties;
	private FixedLoopTime loopTimer = new FixedLoopTime(Settings.SERVER_TICKRATE_MS);
	private RealtimeInterval sendPingInt = new RealtimeInterval(Settings.PING_INTERVAL_MS);
	private RealtimeInterval sendEntityUpdatesInt = new RealtimeInterval(Settings.SERVER_SEND_UPDATE_INTERVAL_MS);
	public BulletAppState bulletAppState;
	private List<MyAbstractMessage> unprocessedMessages = new LinkedList<>();
	private LogWindow logWindow;
	private IConsole console;

	public AbstractGameServer() throws IOException {
		properties = new GameProperties(PROPS_FILE);
		logWindow = new LogWindow("Server", 400, 300);
		console = new ServerConsole(this);
		networkServer = new KryonetServer(this, Settings.TCP_PORT, Settings.UDP_PORT);// SpiderMonkeyServer(this); // todo - move to constructor
	}


	@Override
	public void simpleInitApp() {
		// Set up Physics
		if (Settings.USE_PHYSICS) {
			bulletAppState = new BulletAppState();
			getStateManager().attach(bulletAppState);
			bulletAppState.getPhysicsSpace().addCollisionListener(this);
			//bulletAppState.getPhysicsSpace().addTickListener(this);
		}
		createGame();
		console.appendText("Game created");
		loopTimer.start();
	}


	protected abstract void createGame();


	@Override
	public void simpleUpdate(float tpf_secs) {
		StringBuilder strDebug = new StringBuilder();

		if (networkServer.getNumClients() > 0) {

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
						networkServer.sendMessageToClient(client, new GameSuccessfullyJoinedMessage(client.getPlayerID(), client.avatar.id));
						sendEntityListToClient(client);
						client.clientStatus = ClientData.Status.InGame;

						// Send them a ping to get ping time
						this.networkServer.sendMessageToClient(client, new PingMessage(true));

					} else if (message instanceof UnknownEntityMessage) {
						UnknownEntityMessage uem = (UnknownEntityMessage) message;
						IEntity e = null;
						synchronized (entities) {
							e = this.entities.get(uem.entityID);
						}
						this.sendNewEntity(client, e);

					} else if (message instanceof PlayerLeftMessage) {
						this.connectionRemoved(client.getPlayerID());

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
				this.networkServer.sendMessageToAll(new PingMessage(true));
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
						/*if (sc.type == EntityTypes.AVATAR) {
							AbstractPlayersAvatar av = (AbstractPlayersAvatar)sc;
							strDebug.append("WalkDir: " + av.playerControl.getWalkDirection() + "   Velocity: " + av.playerControl.getVelocity().length() + "\n");
						}*/
						if (sendUpdates) {
							if (sc.hasMoved()) { // Don't send if not moved (unless Avatar)
								networkServer.sendMessageToAll(new EntityUpdateMessage(sc, false));
								//Settings.p("Sending EntityUpdateMessage for " + sc);
							}
						}
					}
				}
			}

			// Loop through clients
			/*synchronized (clients) {
				for (ClientData client : clients.values()) {
				}
			}*/
		}

		this.logWindow.setText(strDebug.toString());

		loopTimer.waitForFinish(); // Keep clients and server running at same speed
		loopTimer.start();
	}


	@Override
	public void messageReceived(int clientid, MyAbstractMessage message) {
		if (Settings.DEBUG_MSGS) {
			Settings.p("Rcvd " + message.getClass().getSimpleName());
		}

		ClientData client = null;
		synchronized (clients) {
			client = clients.get(clientid);
		}

		if (client == null) {
			return;
		}
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
					if ((client.pingRTT/2) + Settings.SERVER_SEND_UPDATE_INTERVAL_MS > Settings.CLIENT_RENDER_DELAY) {
						Settings.p("Warning: client ping is longer than client render delay!");
					}
				} catch (NullPointerException npe) {
					npe.printStackTrace();
				}
			} else {
				// Send it back to the client
				pingMessage.responseSentTime = System.currentTimeMillis();
				this.networkServer.sendMessageToClient(client, pingMessage);
			}

		} else {
			msg.client = client;
			// Add it to list for processing in main thread
			synchronized (this.unprocessedMessages) {
				this.unprocessedMessages.add(msg);
			}
		}

	}


	private ServerPlayersAvatar createPlayersAvatar(ClientData client) {
		int id = getNextEntityID();
		ServerPlayersAvatar avatar = new TestGameServerPlayersAvatar(this, client.getPlayerID(), client.remoteInput, id); // todo - make abstract
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
			this.networkServer.sendMessageToClient(client, aes);
		}
	}


	private void sendNewEntity(ClientData client, IEntity e) {
		if (e instanceof PhysicalEntity) {
			PhysicalEntity se = (PhysicalEntity)e;
			NewEntityMessage nem = new NewEntityMessage(se);
			nem.force = true;
			this.networkServer.sendMessageToClient(client, nem);
		}
	}


	@Override
	public void connectionAdded(int id, Object net) {
		Settings.p("Client connected!");
		ClientData client = new ClientData(id, net, this.getCamera(), this.getInputManager());
		synchronized (clients) {
			clients.put(id, client);
		}
		this.networkServer.sendMessageToClient(client, new WelcomeClientMessage());
	}


	@Override
	public void connectionRemoved(int id) {
		Settings.p("connectionRemoved()");
		synchronized (clients) {
			ClientData client = clients.get(id);
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
				this.networkServer.sendMessageToAll(nem);
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
			this.networkServer.sendMessageToAll(new RemoveEntityMessage(id));
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
	}


	public BulletAppState getBulletAppState() {
		return bulletAppState;
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


	private void rewindAllAvatars(long toTime) {
		synchronized (this.clients) {
			for (ClientData c : this.clients.values()) {
				c.avatar.rewindPositionTo(toTime);
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
			this.networkServer.close();
			this.stop();
		}
	}


	@Override
	public Type getJmeContext() {
		return getContext().getType();
	}


	/*
	 * Returns false if a hit.
	 */
	/*public boolean checkForCollisions(Ray r) {
		boolean result = true;
		CollisionResults res = new CollisionResults();
		synchronized (entities) {
			// Loop through the entities
			for (IEntity e : entities.values()) {
				if (e instanceof ICollideable) {
					ICollideable ic = (ICollideable)e;
					if (r.collideWith(ic.getBoundingVolume(), res) > 0) {
						Settings.p("Collided!");
						return false;
					}
				}
			}
		}
		return result;
	}*/


	@Override
	public Collection getEntities() {
		return Collections.synchronizedCollection(this.entities.values());
		//return this.entities.values().iterator();
	}


	@Override
	public void collisionOccurred(SimpleRigidBody a, Object b) {
		Settings.p("Collision between " + a + " and " + b);
		if (b != null && b instanceof ICollideable) {
			PhysicalEntity pa = (PhysicalEntity)a.getSimplePhysicsEntity();
			pa.collidedWith((ICollideable)b);
		}

	}


}

