package com.scs.stetech1.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.prefs.BackingStoreException;

import ssmith.util.FixedLoopTime;
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
import com.jme3.renderer.Camera;
import com.jme3.system.AppSettings;
import com.scs.stetech1.client.entities.ClientPlayersAvatar;
import com.scs.stetech1.client.entities.PhysicalEntity;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.hud.HUD;
import com.scs.stetech1.input.IInputDevice;
import com.scs.stetech1.input.MouseAndKeyboardCamera;
import com.scs.stetech1.netmessages.EntityUpdateMessage;
import com.scs.stetech1.netmessages.MyAbstractMessage;
import com.scs.stetech1.netmessages.NewEntityMessage;
import com.scs.stetech1.netmessages.NewPlayerAckMessage;
import com.scs.stetech1.netmessages.NewPlayerRequestMessage;
import com.scs.stetech1.netmessages.PingMessage;
import com.scs.stetech1.netmessages.PlayerInputMessage;
import com.scs.stetech1.netmessages.PlayerLeftMessage;
import com.scs.stetech1.netmessages.RemoveEntityMessage;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.AveragePingTime;
import com.scs.stetech1.shared.EntityPositionData;
import com.scs.stetech1.shared.IEntityController;

public class SorcerersClient extends SimpleApplication implements ClientStateListener, ErrorListener<Object>, MessageListener<Client>, IEntityController, PhysicsCollisionListener, ActionListener {

	private static final String QUIT = "Quit";
	private static final String TEST = "Test";

	public static final Random rnd = new Random();

	private RealtimeInterval sendPingInt = new RealtimeInterval(Settings.PING_INTERVAL_MS);

	public static BitmapFont guiFont_small; // = game.getAssetManager().loadFont("Interface/Fonts/Console.fnt");
	public static AppSettings settings;
	private Client myClient;
	public BulletAppState bulletAppState;
	public HashMap<Integer, IEntity> entities = new HashMap<>(100);
	public HUD hud;
	public IInputDevice input;
	public ClientPlayersAvatar avatar;
	public int playerID = -1;
	public long playersAvatarID = -1;
	private AveragePingTime pingCalc = new AveragePingTime();
	public long pingRTT;
	public long clientToServerDiffTime; // Add to current time to get server time

	private RealtimeInterval sendInputsInterval = new RealtimeInterval(Settings.SERVER_TICKRATE_MS);
	private FixedLoopTime loopTimer = new FixedLoopTime(Settings.SERVER_TICKRATE_MS);
	public LinkedList<EntityPositionData> avatarPositionData = new LinkedList<>();
	private List<MyAbstractMessage> messages = new LinkedList<>();

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
			app.setPauseOnLostFocus(false); // Needs to always be in sync with server!

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
			Settings.Register();

			myClient = Network.connectToServer("localhost", Settings.PORT);
			myClient.start();
			myClient.addClientStateListener(this);
			myClient.addErrorListener(this);

			myClient.addMessageListener(this, PingMessage.class);
			myClient.addMessageListener(this, NewEntityMessage.class);
			myClient.addMessageListener(this, EntityUpdateMessage.class);
			myClient.addMessageListener(this, NewPlayerRequestMessage.class);
			myClient.addMessageListener(this, NewPlayerAckMessage.class);
			myClient.addMessageListener(this, RemoveEntityMessage.class);

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


		loopTimer.start();

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
	public void simpleUpdate(float tpf_secs) {  //this.rootNode.getChild(2).getWorldTranslation();
		long serverTime = System.currentTimeMillis() + this.clientToServerDiffTime;

		// Process messages in JME thread
		synchronized (messages) { //this.getCamera()
			// Check we don't already know about it
			while (!this.messages.isEmpty()) {
				MyAbstractMessage message = this.messages.remove(0);
				if (message instanceof NewEntityMessage) {
					NewEntityMessage newEntityMessage = (NewEntityMessage) message;
					if (!this.entities.containsKey(newEntityMessage.entityID)) {
						IEntity e = EntityCreator.createEntity(this, newEntityMessage);
						this.addEntity(e);
					}

				} else if (message instanceof EntityUpdateMessage) {
					EntityUpdateMessage eum = (EntityUpdateMessage)message;
					IEntity e = this.entities.get(eum.entityID);
					if (e != null) {
						Settings.p("Updating " + e);
						EntityPositionData epd = new EntityPositionData();
						epd.serverTimestamp = eum.timestamp + clientToServerDiffTime;
						epd.rotation = eum.dir;
						epd.position = eum.pos;

						PhysicalEntity pe = (PhysicalEntity)e;
						if (eum.force) {
							// Set it now!
							pe.scheduleNewPosition(this, epd.position);
							pe.scheduleNewRotation(this, epd.rotation);
							pe.clearPositiondata();
						} else {
							pe.addPositionData(epd);
						}
						//Settings.p("New position for " + e + ": " + eum.pos);
					} else {
						/*if (this.joinedGame) {
						myClient.send(new UnknownEntityMessage(eum.entityID));  Do we actually need this?
					}*/
					}

				} else if (message instanceof RemoveEntityMessage) {
					RemoveEntityMessage rem = (RemoveEntityMessage)message;
					this.removeEntity(rem.entityID);
				}
			}
		}

		if (myClient.isConnected()) {  //this.rootNode.getChild(1).getWorldTranslation();
			if (sendPingInt.hitInterval()) {
				myClient.send(new PingMessage(false));
			}

			// Send inputs
			if (sendInputsInterval.hitInterval()) { // this.getCamera()
				if (myClient.isConnected() && this.avatar != null) {
					this.myClient.send(new PlayerInputMessage(this.input));

					// Store our position
					EntityPositionData epd = new EntityPositionData();
					epd.serverTimestamp = serverTime;
					epd.position = avatar.getWorldTranslation().clone();
					//epd.rotation not required?
					synchronized (avatarPositionData) {
						avatarPositionData.add(epd);
						// clear these down
						while (avatarPositionData.size() > 10) {
							avatarPositionData.remove(0);
						}
					}
				}
			}
		}

		long serverTimePast = serverTime - Settings.CLIENT_RENDER_DELAY; // Render from history
		for (IEntity e : this.entities.values()) {
			if (e instanceof PhysicalEntity) {
				PhysicalEntity pe = (PhysicalEntity)e;
				if (pe.canMove()) { // Only bother with things that can move
					pe.calcPosition(this, serverTimePast);
				}
			}
		}

		if (this.avatar != null) {
			avatar.process(tpf_secs);
		} else {
			Settings.p("Avatar is null!");
		}

		loopTimer.waitForFinish(); // Keep clients and server running at same speed
		loopTimer.start();

	}


	@Override
	public void messageReceived(Client source, Message message) {
		//Settings.p("Rcvd " + message.getClass().getSimpleName());

		if (message instanceof PingMessage) {
			PingMessage pingMessage = (PingMessage) message;
			if (!pingMessage.s2c) {
				long p2 = System.currentTimeMillis() - pingMessage.originalSentTime;
				this.pingRTT = this.pingCalc.add(p2);
				clientToServerDiffTime = pingMessage.responseSentTime - pingMessage.originalSentTime + (pingRTT/2);
				/*
				 * Client sent time: 1000
				 * Server response time: 5000
				 * Ping: 100 
				 * clientToServerDiffTime: 5000 - 1000 + (100/2) = 4050 
				 */
				Settings.p("pingRTT = " + pingRTT);
				Settings.p("clientToServerDiffTime = " + clientToServerDiffTime);
			} else {
				pingMessage.responseSentTime = System.currentTimeMillis();
				myClient.send(message); // Send it straight back
			}

		} else if (message instanceof NewPlayerAckMessage) {
			NewPlayerAckMessage npcm = (NewPlayerAckMessage)message;
			if (this.playerID <= 0) {
				this.playerID = npcm.playerID;
				this.playersAvatarID = npcm.avatarEntityID;
				synchronized (this.entities) {
					// Set avatar if we already have it
					if (this.entities.containsKey(playersAvatarID)) {
						this.avatar = (ClientPlayersAvatar)entities.get(playersAvatarID);
					}
				}
				Settings.p("We are player " + playerID);
			} else {
				throw new RuntimeException("Already rcvd NewPlayerAckMessage");
			}

		} else if (message instanceof NewEntityMessage) {
			NewEntityMessage newEntityMessage = (NewEntityMessage) message;
			synchronized (messages) {
				messages.add(newEntityMessage);
			}

		} else if (message instanceof EntityUpdateMessage) {
			EntityUpdateMessage eum = (EntityUpdateMessage)message;
			synchronized (messages) {
				messages.add(eum);
			}
			/*IEntity e = this.entities.get(eum.entityID);
			if (e != null) {
				EntityPositionData epd = new EntityPositionData();
				epd.serverTimestamp = eum.timestamp + clientToServerDiffTime;
				epd.rotation = eum.dir;
				epd.position = eum.pos;

				PhysicalEntity pe = (PhysicalEntity)e;
				if (eum.force) {
					// Set it now!
					pe.scheduleNewPosition(this, epd.position);
					pe.scheduleNewRotation(this, epd.rotation);
					pe.clearPositiondata();
				} else {
					pe.addPositionData(epd);
				}
				//Settings.p("New position for " + e + ": " + eum.pos);
			} else {
			}*/

		} else if (message instanceof RemoveEntityMessage) {
			RemoveEntityMessage rem = (RemoveEntityMessage)message;
			synchronized (messages) {
				messages.add(rem);
			}

			//this.removeEntity(rem.entityID);

		} else {
			throw new RuntimeException("Unknown message type: " + message);
		}

		// Always ack all messages!
		/*if (msg.requiresAck) {
			//myClient.send(new AckMessage(msg.msgId)); // Send it straight back
		}*/
	}


	@Override
	public void clientConnected(Client arg0) {
		Settings.p("Connected!");

	}


	@Override
	public void clientDisconnected(Client arg0, DisconnectInfo arg1) {
		Settings.p("Disconnected!");

	}


	@Override
	public void handleError(Object obj, Throwable ex) {
		Settings.p("Network error with " + obj + ": " + ex);
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

		/*PhysicalEntity a=null, b=null;
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
		synchronized (entities) {
			this.entities.put(e.getID(), e);
		}
	}


	public void removeEntity(int id) {
		synchronized (entities) {
			this.entities.remove(id);
		}
	}


	public BulletAppState getBulletAppState() {
		return bulletAppState;
	}


	@Override
	public void onAction(String name, boolean value, float tpf) {
		if (name.equalsIgnoreCase(QUIT)) {
			if (playerID >= 0) {
				this.myClient.send(new PlayerLeftMessage(this.playerID));
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				this.myClient.close();
				this.stop();
			}
		} else if (name.equalsIgnoreCase(TEST)) {

		}
	}


	@Override
	public boolean isServer() {
		return false;
	}


	@Override
	public IEntity getPlayersAvatar() {
		return avatar;
	}


}
