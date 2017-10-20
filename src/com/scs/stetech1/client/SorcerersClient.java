package com.scs.stetech1.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.prefs.BackingStoreException;

import ssmith.util.RealtimeInterval;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.font.BitmapFont;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.ErrorListener;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.serializing.Serializer;
import com.jme3.renderer.Camera;
import com.jme3.system.AppSettings;
import com.scs.stetech1.client.entities.PhysicalEntity;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.hud.HUD;
import com.scs.stetech1.input.IInputDevice;
import com.scs.stetech1.input.MouseAndKeyboardCamera;
import com.scs.stetech1.netmessages.EntityUpdateMessage;
import com.scs.stetech1.netmessages.HelloMessage;
import com.scs.stetech1.netmessages.MyAbstractMessage;
import com.scs.stetech1.netmessages.NewEntityMessage;
import com.scs.stetech1.netmessages.NewPlayerAckMessage;
import com.scs.stetech1.netmessages.NewPlayerRequestMessage;
import com.scs.stetech1.netmessages.PingMessage;
import com.scs.stetech1.netmessages.PlayerInputMessage;
import com.scs.stetech1.netmessages.UnknownEntityMessage;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.IEntityController;
import com.scs.stetech1.shared.SharedSettings;

public class SorcerersClient extends SimpleApplication implements ClientStateListener, ErrorListener<Object>, MessageListener<Client>, IEntityController, PhysicsCollisionListener, ActionListener {

	private static final String QUIT = "Quit";
	private static final String TEST = "Test";

	public static final Random rnd = new Random();

	public static BitmapFont guiFont_small; // = game.getAssetManager().loadFont("Interface/Fonts/Console.fnt");
	//public static SorcerersClient instance;
	public static AppSettings settings;
	private Client myClient;
	public BulletAppState bulletAppState;
	public HashMap<Integer, IEntity> entities = new HashMap<>(100);
	public HUD hud;
	public IInputDevice input;
	private boolean joinedGame = false; // todo - set to true when conf'd
	
	//private PacketCache packets = new PacketCache();
	private RealtimeInterval sendInputsInterval = new RealtimeInterval(50);

	public static void main(String[] args) {
		try {
			settings = new AppSettings(true);
			try {
				settings.load(Settings.NAME);
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
			settings.setUseJoysticks(true);
			settings.setTitle(Settings.NAME + " (v" + Settings.VERSION + ")");
			if (Settings.SHOW_LOGO) {
				//settings.setSettingsDialogImage("/game_logo.png");
			} else {
				settings.setSettingsDialogImage(null);
			}

			SorcerersClient app = new SorcerersClient();
			//instance = app;
			app.setSettings(settings);
			app.setPauseOnLostFocus(true);

			/*File video, audio;
			if (Settings.RECORD_VID) {
				//app.setTimer(new IsoTimer(60));
				video = File.createTempFile("JME-water-video", ".avi");
				audio = File.createTempFile("JME-water-audio", ".wav");
				Capture.captureVideo(app, video);
				Capture.captureAudio(app, audio);
			}*/

			app.start();

			/*if (Settings.RECORD_VID) {
				System.out.println("Video saved at " + video.getCanonicalPath());
				System.out.println("Audio saved at " + audio.getCanonicalPath());
			}*/

			try {
				settings.save(Settings.NAME);
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			Settings.p("Error: " + e);
			e.printStackTrace();
		}

	}


	@Override
	public void simpleInitApp() {
		// Clear existing mappings
		getInputManager().clearMappings();
		getInputManager().clearRawInputListeners();

		assetManager.registerLocator("assets/", FileLocator.class); // default
		assetManager.registerLocator("assets/", ClasspathLocator.class);

		//guiFont_small = getAssetManager().loadFont("Interface/Fonts/Console.fnt");
		guiFont_small = getAssetManager().loadFont("Interface/Fonts/Console.fnt");

		// Set up Physics
		bulletAppState = new BulletAppState();
		getStateManager().attach(bulletAppState);
		bulletAppState.getPhysicsSpace().addCollisionListener(this);
		//bulletAppState.getPhysicsSpace().addTickListener(this);
		//bulletAppState.getPhysicsSpace().setAccuracy(1f / 80f);
		//bulletAppState.getPhysicsSpace().enableDebug(game.getAssetManager());

		try {
			Serializer.registerClass(HelloMessage.class);
			Serializer.registerClass(PingMessage.class);
			//Serializer.registerClass(AckMessage.class);
			Serializer.registerClass(NewPlayerRequestMessage.class);
			Serializer.registerClass(NewPlayerAckMessage.class);
			Serializer.registerClass(PlayerInputMessage.class);
			Serializer.registerClass(UnknownEntityMessage.class);
			Serializer.registerClass(NewEntityMessage.class);
			Serializer.registerClass(EntityUpdateMessage.class);

			myClient = Network.connectToServer("localhost", 6143);
			myClient.start();
			myClient.addClientStateListener(this);
			myClient.addErrorListener(this);

			myClient.addMessageListener(this, HelloMessage.class);
			myClient.addMessageListener(this, PingMessage.class);
			//myClient.addMessageListener(this, AckMessage.class);
			myClient.addMessageListener(this, NewEntityMessage.class);
			myClient.addMessageListener(this, EntityUpdateMessage.class);
			myClient.addMessageListener(this, NewPlayerRequestMessage.class);
			myClient.addMessageListener(this, NewPlayerAckMessage.class);

			myClient.send(new HelloMessage("123"));
			myClient.send(new NewPlayerRequestMessage("Mark Gray"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.01f, Settings.CAM_DIST);

		//getCamera().setLocation(new Vector3f(0f, 0f, 10f));
		//game.getCamera().lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);

		getInputManager().addMapping(QUIT, new KeyTrigger(KeyInput.KEY_ESCAPE));
		getInputManager().addListener(this, QUIT);            

		getInputManager().addMapping(TEST, new KeyTrigger(KeyInput.KEY_T));
		getInputManager().addListener(this, TEST);            

		setUpLight();

		hud = this.createHUD(getCamera(), 0);
		input = new MouseAndKeyboardCamera(getCamera(), getInputManager());
		//this.addPlayersAvatar(0, getCamera(), input, hud);

		if (Settings.RECORD_VID) {
			Settings.p("Recording video");
			VideoRecorderAppState video_recorder = new VideoRecorderAppState();
			stateManager.attach(video_recorder);
		}

	}


	private HUD createHUD(Camera c, int id) {
		BitmapFont guiFont_small = getAssetManager().loadFont("Interface/Fonts/Console.fnt");
		// HUD coords are full screen co-ords!
		// cam.getWidth() = 640x480, cam.getViewPortLeft() = 0.5f
		float xBL = c.getWidth() * c.getViewPortLeft();
		//float y = (c.getHeight() * c.getViewPortTop())-(c.getHeight()/2);
		float yBL = c.getHeight() * c.getViewPortBottom();

		//Settings.p("Created HUD for " + id + ": " + xBL + "," +yBL);

		float w = c.getWidth() * (c.getViewPortRight()-c.getViewPortLeft());
		float h = c.getHeight() * (c.getViewPortTop()-c.getViewPortBottom());
		HUD hud = new HUD(this, xBL, yBL, w, h, guiFont_small, id, c);
		getGuiNode().attachChild(hud);
		return hud;

	}


	private void setUpLight() {
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(2));
		getRootNode().addLight(al);
	}


	@Override
	public void simpleUpdate(float tpf_secs) {
		// Send inputs every 50ms
		if (sendInputsInterval.hitInterval()) {
			// Send packets
			/*Iterator<MyAbstractMessage> it = this.packets.getMsgs();
			while (it.hasNext()) {
				this.myClient.send(it.next());
			}*/
			this.myClient.send(new PlayerInputMessage(this.input));
		}
	}


	@Override
	public void messageReceived(Client source, Message message) {
		MyAbstractMessage msg = (MyAbstractMessage)message;
		/*if (msg.requiresAck) {
			// Check not already been ack'd
			if (packets.hasBeenAckd(msg.msgId)) {
				return;
			}
		}*/

		if (message instanceof PingMessage) {
			//PingMessage pingMessage = (PingMessage) message;
			myClient.send(message); // Send it straight back
		} else if (message instanceof HelloMessage) {
			HelloMessage helloMessage = (HelloMessage) message;
			System.out.println("Client #"+source.getId()+" received: '"+helloMessage.getMessage() + "'");
		/*} else if (message instanceof AckMessage) {
			AckMessage ackMessage = (AckMessage) message;
			this.packets.acked(ackMessage.ackingId);*/
		} else if (message instanceof NewEntityMessage) {
			NewEntityMessage newEntityMessage = (NewEntityMessage) message;
			IEntity e = EntityCreator.createEntity(this, newEntityMessage);
			this.addEntity(e);
		} else if (message instanceof EntityUpdateMessage) {
			EntityUpdateMessage eum = (EntityUpdateMessage)message;
			IEntity e = this.entities.get(eum.entityID);
			if (e != null) {
				PhysicalEntity pe = (PhysicalEntity)e;
				pe.getMainNode().setLocalTranslation(eum.pos);
			} else {
				if (this.joinedGame) {
					//this.packets.add(new UnknownEntityMessage(eum.entityID));
					myClient.send(new UnknownEntityMessage(eum.entityID));
				}
			}
		} else {
			throw new RuntimeException("Unknown message type: " + message);
		}

		// Always ack all messages!
		if (msg.requiresAck) {
			//myClient.send(new AckMessage(msg.msgId)); // Send it straight back
		}
	}


	@Override
	public void clientConnected(Client arg0) {
		SharedSettings.p("Connected!");

	}


	@Override
	public void clientDisconnected(Client arg0, DisconnectInfo arg1) {
		SharedSettings.p("Disconnected!");

	}


	@Override
	public void handleError(Object obj, Throwable ex) {
		SharedSettings.p("Network error with " + obj + ": " + ex);
		ex.printStackTrace();

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


	public void addEntity(IEntity e) {
		this.entities.put(e.getID(), e);

	}


	public void removeEntity(IEntity e) {
		this.entities.remove(e.getID());

	}


	public BulletAppState getBulletAppState() {
		return bulletAppState;
	}


	@Override
	public void onAction(String name, boolean value, float tpf) {
	}


	@Override
	public boolean isServer() {
		return false;
	}


}
