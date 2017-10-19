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
import com.scs.stetech1.netmessages.AckMessage;
import com.scs.stetech1.netmessages.EntityUpdateMessage;
import com.scs.stetech1.netmessages.HelloMessage;
import com.scs.stetech1.netmessages.MyAbstractMessage;
import com.scs.stetech1.netmessages.NewEntityMessage;
import com.scs.stetech1.netmessages.NewPlayerMessage;
import com.scs.stetech1.netmessages.PingMessage;
import com.scs.stetech1.server.entities.ServerPlayersAvatar;
import com.scs.stetech1.shared.IEntityController;
import com.scs.stetech1.shared.AbstractPlayersAvatar;
import com.scs.stetech1.shared.SharedSettings;

public class ServerMain extends SimpleApplication implements IEntityController, ConnectionListener, ErrorListener, MessageListener<HostedConnection>, PhysicsCollisionListener  {

	private static final String PROPS_FILE = Settings.NAME.replaceAll(" ", "") + "_settings.txt";

	private Server myServer;
	private HashMap<Integer, ClientData> clients = new HashMap<>(10);
	public HashMap<Integer, IEntity> entities = new HashMap<>(100);

	public static SorcerersProperties properties;
	private FixedLoopTime loopTimer = new FixedLoopTime(100);
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

		myServer = Network.createServer(SharedSettings.PORT);
		myServer.start();
		myServer.addConnectionListener(this);

		Serializer.registerClass(HelloMessage.class);
		myServer.addMessageListener(this, HelloMessage.class);

		Serializer.registerClass(PingMessage.class);
		myServer.addMessageListener(this, PingMessage.class);

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
		myServer.broadcast(new PingMessage());

		// Loop through the ents
		for (IEntity e : entities.values()) {
			if (e instanceof IProcessable) {
				IProcessable p = (IProcessable)e;
				p.process(tpf_secs);
			}
			if (e instanceof ISharedEntity) {
				ISharedEntity sc = (ISharedEntity)e;
				if (sc.canMove()) {
					myServer.broadcast(new EntityUpdateMessage(sc));
				}
			}
		}

		// Loop through clients
		synchronized (clients) {
			for (ClientData client : clients.values()) {
				client.sendMessages(this.myServer);
			}
		}

		loopTimer.waitForFinish();
		loopTimer.start();
	}


	@Override
	public void messageReceived(HostedConnection source, Message message) {
		ClientData client = clients.get(source.getId());

		MyAbstractMessage msg = (MyAbstractMessage)message;
		if (msg.requiresAck) {
			// Check not already been ack'd
			if (client.packets.hasBeenAckd(msg.id)) {
				return;
			}
		}

		if (message instanceof PingMessage) {
			PingMessage pingMessage = (PingMessage) message;
			client.ping = System.nanoTime() - pingMessage.sentTime;
		} else if (message instanceof HelloMessage) {
			HelloMessage helloMessage = (HelloMessage) message;
			System.out.println("Server received '" +helloMessage.getMessage() +"' from client #"+source.getId() );
		} else if (message instanceof NewPlayerMessage) {
			NewPlayerMessage newPlayerMessage = (NewPlayerMessage) message;
			client.name = newPlayerMessage.name;
			createPlayersAvatar(client);
			sendEntityListToClient(client);
		} else if (message instanceof AckMessage) {
			AckMessage ackMessage = (AckMessage) message;
			client.packets.acked(ackMessage.ackingId);
		} else {
			throw new RuntimeException("Unknown message type: " + message);
		}

		// Alway ack all messages!
		if (msg.requiresAck) {
			myServer.broadcast(Filters.equalTo(client.conn), new AckMessage(msg.id)); // Send it straight back
		}
	}


	private void createPlayersAvatar(ClientData client) {
		AbstractPlayersAvatar avatar = new ServerPlayersAvatar(this, client.id, client.remoteInput);
		this.addEntity(avatar);
	}


	private void sendEntityListToClient(ClientData client) {
		synchronized (entities) {
			for (IEntity e : entities.values()) {
				if (e instanceof ISharedEntity) {
					ISharedEntity se = (ISharedEntity)e;
					client.packets.add(new NewEntityMessage(se));
					//client.packets.add(new EntityUpdateMessage(se));
				}
			}
		}
	}


	@Override
	public void handleError(Object arg0, Throwable ex) {
		SharedSettings.p("Network error: " + ex);

	}


	@Override
	public void connectionAdded(Server arg0, HostedConnection conn) {
		SharedSettings.p("Client connected!");
		clients.put(conn.getId(), new ClientData(conn));

	}


	@Override
	public void connectionRemoved(Server arg0, HostedConnection source) {
		SharedSettings.p("Client removed");

		ClientData client = clients.get(source.getId());

		this.playerLeft(client.id);
	}


	private void playerLeft(int id) {
		synchronized (clients) {
			this.clients.remove(id);
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
		Floor floor = new Floor(this, 0, 0, 0, 10, 10, .5f, "", null);
		this.addEntity(floor);

		Crate crate = new Crate(this, 0, 0, 0, 10, 10, .5f, "", 0);
		this.addEntity(crate);
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



}
