package com.scs.stetech1.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import ssmith.util.FixedLoopTime;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.network.ConnectionListener;
import com.jme3.network.ErrorListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.serializing.Serializer;
import com.jme3.system.JmeContext;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.components.IServerControlled;
import com.scs.stetech1.netmessages.HelloMessage;
import com.scs.stetech1.netmessages.PingMessage;
import com.scs.stetech1.shared.IEntityController;
import com.scs.stetech1.shared.SharedSettings;

public class ServerMain extends SimpleApplication implements IEntityController, ConnectionListener, ErrorListener, MessageListener<HostedConnection>, PhysicsCollisionListener  {

	private static final String PROPS_FILE = Settings.NAME.replaceAll(" ", "") + "_settings.txt";

	private Server myServer;
	public static SorcerersProperties properties;
	private HashMap<Integer, ClientData> clients = new HashMap<>(10);
	private FixedLoopTime loopTimer = new FixedLoopTime(100);
	//private ServerGame game;
	public HashMap<Integer, IEntity> entities = new HashMap<>(100);
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
		//myServer.broadcast(arg0)

	}


	@Override
	public void simpleInitApp() {
		// Set up Physics
		bulletAppState = new BulletAppState();
		getStateManager().attach(bulletAppState);
		bulletAppState.getPhysicsSpace().addCollisionListener(this);
		//bulletAppState.getPhysicsSpace().addTickListener(this);
		//bulletAppState.getPhysicsSpace().setAccuracy(1f / 80f);
	}
	
	
	public void gameLoop(float tpf_secs) {
		myServer.broadcast(new PingMessage());

		// Loop through the ents
		for (IEntity e : entities.values()) {
			if (e instanceof IServerControlled) {
				IServerControlled sc = (IServerControlled)e;
				sc.process(tpf_secs);
			}
		}

		/*if (game != null) {
			game.gameLoop(tpf_secs);
		}*/

		// Loop through clients
		for (ClientData client : clients.values()) {
			for (IEntity e : entities.values()) {
				// todo - send entity updates to all
			}
		}

		loopTimer.waitForFinish();
		loopTimer.start();
	}


	@Override
	public void messageReceived(HostedConnection source, Message message) {
		ClientData client = clients.get(source.getId());
		if (message instanceof PingMessage) {
			PingMessage pingMessage = (PingMessage) message;
			client.ping = System.nanoTime() - pingMessage.sentTime;
		} else if (message instanceof HelloMessage) {
			HelloMessage helloMessage = (HelloMessage) message;
			System.out.println("Server received '" +helloMessage.getMessage() +"' from client #"+source.getId() );
		} else {
			throw new RuntimeException("Unknown message type: " + message);
		}
	}


	@Override
	public void handleError(Object arg0, Throwable arg1) {
		SharedSettings.p("Network error: " + arg1);
		// TODO Auto-generated method stub

	}


	@Override
	public void connectionAdded(Server arg0, HostedConnection arg1) {
		SharedSettings.p("Client connected!");
		clients.put(arg1.getId(), new ClientData(arg1.getId(), arg1));

		//todo - add avatar
		
		/*if (game == null && clients.size() > 0) {
			game = new ServerGame();
		}*/
	}


	@Override
	public void connectionRemoved(Server arg0, HostedConnection arg1) {
		SharedSettings.p("Client removed");
		// todo - remove client
		// todo - remove avatar
		// TODO Auto-generated method stub

	}
	
	
	@Override
	public void addEntity(IEntity e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void removeEntity(IEntity e) {
		// TODO Auto-generated method stub
		
	}


	public BulletAppState getBulletAppState() {
		return bulletAppState;
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
