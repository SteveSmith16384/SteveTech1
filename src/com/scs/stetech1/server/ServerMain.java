package com.scs.stetech1.server;

import java.io.IOException;
import java.util.HashMap;

import ssmith.util.FixedLoopTime;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.network.ConnectionListener;
import com.jme3.network.ErrorListener;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.serializing.Serializer;
import com.jme3.system.JmeContext;
import com.scs.stetech1.client.entities.Crate;
import com.scs.stetech1.client.entities.Floor;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.components.IProcessable;
import com.scs.stetech1.components.ISharedEntity;
import com.scs.stetech1.netmessages.EntityUpdateMessage;
import com.scs.stetech1.netmessages.HelloMessage;
import com.scs.stetech1.netmessages.MyAbstractMessage;
import com.scs.stetech1.netmessages.NewEntityMessage;
import com.scs.stetech1.netmessages.NewPlayerAckMessage;
import com.scs.stetech1.netmessages.NewPlayerRequestMessage;
import com.scs.stetech1.netmessages.PingMessage;
import com.scs.stetech1.netmessages.PlayerInputMessage;
import com.scs.stetech1.netmessages.UnknownEntityMessage;
import com.scs.stetech1.server.entities.ServerPlayersAvatar;
import com.scs.stetech1.shared.IEntityController;
import com.scs.stetech1.shared.SharedSettings;

public class ServerMain extends SimpleApplication implements IEntityController, ConnectionListener, ErrorListener, MessageListener<HostedConnection>, PhysicsCollisionListener  {

	private static final String PROPS_FILE = Settings.NAME.replaceAll(" ", "") + "_settings.txt";

	private Server myServer;
	private HashMap<Integer, ClientData> clients = new HashMap<>(10); // PlayerID::ClientData
	public HashMap<Integer, IEntity> entities = new HashMap<>(100); // EntityID::Entity

	public static SorcerersProperties properties;
	private FixedLoopTime loopTimer = new FixedLoopTime(100);
	private RealtimeInterval sendEntityUpdatesInt = new RealtimeInterval(SharedSettings.SERVER_SEND_UPDATE_INTERVAL_MS);
	public BulletAppState bulletAppState;

	public static void main(String[] args) {
		try {
			ServerMain app;
			app = new ServerMain();
			app.start(JmeContext.Type.Headless);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public ServerMain() throws IOException {
		properties = new SorcerersProperties(PROPS_FILE);
		loopTimer.start();

		Serializer.registerClass(HelloMessage.class);
		Serializer.registerClass(PingMessage.class);
		//Serializer.registerClass(AckMessage.class);
		Serializer.registerClass(NewPlayerRequestMessage.class);
		Serializer.registerClass(NewPlayerAckMessage.class);
		Serializer.registerClass(PlayerInputMessage.class);
		Serializer.registerClass(UnknownEntityMessage.class);
		Serializer.registerClass(NewEntityMessage.class);
		Serializer.registerClass(EntityUpdateMessage.class);

		myServer = Network.createServer(SharedSettings.PORT);
		myServer.start();
		myServer.addConnectionListener(this);

		myServer.addMessageListener(this, HelloMessage.class);
		myServer.addMessageListener(this, PingMessage.class);
		//myServer.addMessageListener(this, AckMessage.class);
		myServer.addMessageListener(this, NewPlayerRequestMessage.class);
		myServer.addMessageListener(this, NewPlayerAckMessage.class);
		myServer.addMessageListener(this, PlayerInputMessage.class);
		myServer.addMessageListener(this, UnknownEntityMessage.class);
		myServer.addMessageListener(this, NewEntityMessage.class);
		myServer.addMessageListener(this, EntityUpdateMessage.class);

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
	}


	@Override
	public void simpleUpdate(float tpf_secs) {
		if (myServer.hasConnections()) { // this.rootNode
			//myServer.broadcast(new PingMessage()); todo - re-add

			// Loop through the ents
			for (IEntity e : entities.values()) {
				if (e instanceof IProcessable) {
					IProcessable p = (IProcessable)e;
					p.process(tpf_secs);
				}
				if (sendEntityUpdatesInt.hasHit()) {
				if (e instanceof ISharedEntity) {
					ISharedEntity sc = (ISharedEntity)e;
					if (sc.canMove()) {
						//todo - re-add myServer.broadcast(new EntityUpdateMessage(sc));
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

		loopTimer.waitForFinish();
		loopTimer.start();
	}


	@Override
	public void messageReceived(HostedConnection source, Message message) {
		ClientData client = null;
		synchronized (clients) {
			client = clients.get(source.getId());
		}

		MyAbstractMessage msg = (MyAbstractMessage)message;
		/*if (msg.requiresAck) {
			// Check not already been ack'd
			try {
				if (client.packets.hasBeenAckd(msg.msgId)) {
					return;
				}
			} catch (NullPointerException ex) {
				ex.printStackTrace();
			}
		}*/

		if (message instanceof PingMessage) {
			PingMessage pingMessage = (PingMessage) message;
			client.ping = System.currentTimeMillis() - pingMessage.sentTime;
			
		} else if (message instanceof PlayerInputMessage) {
			PlayerInputMessage pim = (PlayerInputMessage)message;
			if (pim.timestamp > client.latestInputTimestamp) {
				client.remoteInput.decodeMessage(pim);
				client.latestInputTimestamp = pim.timestamp;
			}
		} else if (message instanceof HelloMessage) {
			HelloMessage helloMessage = (HelloMessage) message;
			System.out.println("Server received '" +helloMessage.getMessage() +"' from client #"+source.getId() );
			
		} else if (message instanceof NewPlayerRequestMessage) {
			NewPlayerRequestMessage newPlayerMessage = (NewPlayerRequestMessage) message;
			client.name = newPlayerMessage.name;
			int avatarid = createPlayersAvatar(client);
			// Send newplayerconf message
			myServer.broadcast(Filters.equalTo(client.conn), new NewPlayerAckMessage(client.getPlayerID(), avatarid));
			sendEntityListToClient(client);
			
		/*} else if (message instanceof AckMessage) {
			AckMessage ackMessage = (AckMessage) message;
			client.packets.acked(ackMessage.ackingId);*/

		} else if (message instanceof UnknownEntityMessage) {
			UnknownEntityMessage uem = (UnknownEntityMessage)msg;
			IEntity e = this.entities.get(uem.entityID);
			this.sendNewEntity(client, e);

		} else {
			throw new RuntimeException("Unknown message type: " + message);
		}

		/*if (msg.requiresAck) {
			//myServer.broadcast(Filters.equalTo(client.conn), new AckMessage(msg.msgId)); // Send it straight back
		}*/
	}


	private int createPlayersAvatar(ClientData client) {
		ServerPlayersAvatar avatar = new ServerPlayersAvatar(this, client.getPlayerID(), client.remoteInput);
		avatar.moveToStartPostion(true);
		this.addEntity(avatar);
		return avatar.id;
	}


	private void sendEntityListToClient(ClientData client) {
		synchronized (entities) {
			for (IEntity e : entities.values()) {
				this.sendNewEntity(client, e);
			}
		}
	}


	private void sendNewEntity(ClientData client, IEntity e) {
		if (e instanceof ISharedEntity) {
			ISharedEntity se = (ISharedEntity)e;
			//client.packets.add(new NewEntityMessage(se));
			myServer.broadcast(Filters.equalTo(client.conn), new NewEntityMessage(se));

		}

	}


	@Override
	public void handleError(Object obj, Throwable ex) {
		SharedSettings.p("Network error with " + obj + ": " + ex);
		ex.printStackTrace();

	}


	@Override
	public void connectionAdded(Server arg0, HostedConnection conn) {
		SharedSettings.p("Client connected!");
		synchronized (clients) {
			clients.put(conn.getId(), new ClientData(conn));
		}

	}


	@Override
	public void connectionRemoved(Server arg0, HostedConnection source) {
		SharedSettings.p("Client removed");

		ClientData client = clients.get(source.getId());

		this.playerLeft(client);
	}


	private void playerLeft(ClientData client) {
		synchronized (clients) {
			this.clients.remove(client.getPlayerID());
		}
		// todo - remove avatar

	}


	@Override
	public void addEntity(IEntity e) {
		synchronized (entities) {
			this.entities.put(e.getID(), e);
		}

	}


	@Override
	public void removeEntity(IEntity e) {
		synchronized (entities) {
			this.entities.remove(e.getID());
		}

	}


	public BulletAppState getBulletAppState() {
		return bulletAppState;
	}



	private void createGame() {
		Floor floor = new Floor(this, 0, 0, 0, 10, .5f, 10, "Textures/crate.png", null);
		this.addEntity(floor);

		Crate crate = new Crate(this, 8, 2, 8, 1, 1, 1f, "Textures/floor015.png", 0);
		this.addEntity(crate); // this.rootNode
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



}
