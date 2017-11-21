package com.scs.stetech1.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext.Type;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.components.IProcessByClient;
import com.scs.stetech1.entities.AbstractPlayersAvatar;
import com.scs.stetech1.entities.ClientPlayersAvatar;
import com.scs.stetech1.entities.PhysicalEntity;
import com.scs.stetech1.hud.HUD;
import com.scs.stetech1.input.IInputDevice;
import com.scs.stetech1.input.MouseAndKeyboardCamera;
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
import com.scs.stetech1.networking.IMessageClient;
import com.scs.stetech1.networking.IMessageClientListener;
import com.scs.stetech1.networking.SpiderMonkeyClient;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.AverageNumberCalculator;
import com.scs.stetech1.shared.EntityPositionData;
import com.scs.stetech1.shared.IEntityController;
import com.scs.stetech1.shared.PositionCalculator;

import ssmith.util.FixedLoopTime;
import ssmith.util.RealtimeInterval;

public abstract class GenericClient extends SimpleApplication implements IEntityController, PhysicsCollisionListener, ActionListener, IMessageClientListener { // PhysicsTickListener, 

	private static final String QUIT = "Quit";
	private static final String TEST = "Test";

	private RealtimeInterval sendPingInt = new RealtimeInterval(Settings.PING_INTERVAL_MS);

	public static BitmapFont guiFont_small; // = game.getAssetManager().loadFont("Interface/Fonts/Console.fnt");
	public static AppSettings settings;
	private IMessageClient networkClient;
	public BulletAppState bulletAppState;
	public HashMap<Integer, IEntity> entities = new HashMap<>(100);
	public HUD hud;
	public IInputDevice input;
	
	public ClientPlayersAvatar avatar;
	public int playerID = -1;
	public long playersAvatarID = -1;
	private AverageNumberCalculator pingCalc = new AverageNumberCalculator();
	public long pingRTT;
	public long clientToServerDiffTime; // Add to current time to get server time
	public boolean gameStarted = false;
	private boolean acceptByServer = false;

	private RealtimeInterval sendInputsInterval = new RealtimeInterval(Settings.SERVER_TICKRATE_MS);
	private FixedLoopTime loopTimer = new FixedLoopTime(Settings.SERVER_TICKRATE_MS);
	public PositionCalculator clientAvatarPositionData = new PositionCalculator(true, 500);
	private List<MyAbstractMessage> unprocessedMessages = new LinkedList<>();

	protected GenericClient() {
		super();
	}
	
	
	@Override
	public void simpleInitApp() {
		// Clear existing mappings
		getInputManager().clearMappings();
		getInputManager().clearRawInputListeners();

		assetManager.registerLocator("assets/", FileLocator.class); // default
		assetManager.registerLocator("assets/", ClasspathLocator.class);

		guiFont_small = getAssetManager().loadFont("Interface/Fonts/Console.fnt");

		// Set up Physics
		bulletAppState = new BulletAppState();
		getStateManager().attach(bulletAppState);
		bulletAppState.getPhysicsSpace().addCollisionListener(this);
		//bulletAppState.getPhysicsSpace().addTickListener(this);
		//bulletAppState.getPhysicsSpace().enableDebug(this.getAssetManager());
		bulletAppState.setEnabled(false); // Wait until all entities received

		cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.01f, Settings.CAM_DIST);

		getInputManager().addMapping(QUIT, new KeyTrigger(KeyInput.KEY_ESCAPE));
		getInputManager().addListener(this, QUIT);            

		getInputManager().addMapping(TEST, new KeyTrigger(KeyInput.KEY_T));
		getInputManager().addListener(this, TEST);            

		setUpLight();

		hud = this.createHUD(getCamera());
		input = new MouseAndKeyboardCamera(getCamera(), getInputManager());

		if (Settings.RECORD_VID) {
			Settings.p("Recording video");
			VideoRecorderAppState video_recorder = new VideoRecorderAppState();
			stateManager.attach(video_recorder);
		}

		try {
			networkClient = new SpiderMonkeyClient(this); // todo -move to constructor
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}

		loopTimer.start();

	}


	private HUD createHUD(Camera c) {
		BitmapFont guiFont_small = getAssetManager().loadFont("Interface/Fonts/Console.fnt");
		// HUD coords are full screen co-ords!
		// cam.getWidth() = 640x480, cam.getViewPortLeft() = 0.5f
		float xBL = c.getWidth() * c.getViewPortLeft();
		//float y = (c.getHeight() * c.getViewPortTop())-(c.getHeight()/2);
		float yBL = c.getHeight() * c.getViewPortBottom();

		float w = c.getWidth() * (c.getViewPortRight()-c.getViewPortLeft());
		float h = c.getHeight() * (c.getViewPortTop()-c.getViewPortBottom());
		HUD hud = new HUD(this, xBL, yBL, w, h, guiFont_small, c);
		getGuiNode().attachChild(hud);
		return hud;

	}


	private void setUpLight() {
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(1));
		getRootNode().addLight(al);
	}


	@Override
	public void simpleUpdate(float tpf_secs) {  //this.rootNode.getChild(3).getWorldTranslation();
		try { //this.entities;
			final long serverTime = System.currentTimeMillis() + this.clientToServerDiffTime;

			if (networkClient != null && networkClient.isConnected()) {
				// Process messages in JME thread
				synchronized (unprocessedMessages) {
					// Check we don't already know about it
					while (!this.unprocessedMessages.isEmpty()) {
						MyAbstractMessage message = this.unprocessedMessages.remove(0);
						if (message instanceof NewEntityMessage) {
							NewEntityMessage newEntityMessage = (NewEntityMessage) message;
							if (!this.entities.containsKey(newEntityMessage.entityID)) {
								IEntity e = createEntity(newEntityMessage);
								this.addEntity(e);
							} else {
								// We already know about it
							}

						} else if (message instanceof EntityUpdateMessage) {
							EntityUpdateMessage eum = (EntityUpdateMessage)message;
							IEntity e = this.entities.get(eum.entityID);
							if (e != null) {
								//Settings.p("Received EntityUpdateMessage for " + e);
								EntityPositionData epd = new EntityPositionData();
								epd.serverTimestamp = eum.timestamp;// + clientToServerDiffTime;
								epd.rotation = eum.dir;
								epd.position = eum.pos;

								PhysicalEntity pe = (PhysicalEntity)e;
								if (eum.force) {
									// Set it now!
									pe.setWorldTranslation(epd.position);
									pe.setWorldRotation(epd.rotation);
									pe.clearPositiondata();
									if (pe == this.avatar) {
										this.clientAvatarPositionData.clearPositiondata(); // Clear our local data as well
										storeAvatarPosition(serverTime);
										// Stop us walking!
										this.avatar.resetWalkDir();
									}
								}
								pe.addPositionData(epd); // Store the position for use later
								//Settings.p("New position for " + e + ": " + eum.pos);
							} else {
								Settings.p("Unknown entity ID: " + eum.entityID);
								// Ask the server for entity details since we don't know about it.
								networkClient.sendMessageToServer(new UnknownEntityMessage(eum.entityID));
							}

						} else if (message instanceof RemoveEntityMessage) {
							RemoveEntityMessage rem = (RemoveEntityMessage)message;
							this.removeEntity(rem.entityID);

						} else if (message instanceof GeneralCommandMessage) { // We now have enough data to start
							this.bulletAppState.setEnabled(true); // Go!
							gameStarted = true;

						} else {
							throw new RuntimeException("Unknown message type: " + message);
						}
					}
				}

				if (acceptByServer && sendPingInt.hitInterval()) {
					networkClient.sendMessageToServer(new PingMessage(false));
				}

				if (gameStarted) {
					if (this.avatar != null) {
						// Send inputs
						if (sendInputsInterval.hitInterval()) {
							if (networkClient.isConnected()) {
								this.networkClient.sendMessageToServer(new PlayerInputMessage(this.input));
							}
						}
						storeAvatarPosition(serverTime);
					}

					long serverTimePast = serverTime - Settings.CLIENT_RENDER_DELAY; // Render from history

					/*if (this.avatar != null) {
						//avatar.resetWalkDir(); // todo - do this in one place
					}*/

					// Loop through each entity and calc correct position				
					StringBuffer strListEnts = new StringBuffer(); // Log entities
					for (IEntity e : this.entities.values()) {
						if (e instanceof PhysicalEntity) {
							PhysicalEntity pe = (PhysicalEntity)e;
							strListEnts.append(pe.name + ": " + pe.getWorldTranslation() + "\n");
							if (pe instanceof AbstractPlayersAvatar) {
								AbstractPlayersAvatar av = (AbstractPlayersAvatar)pe;
								strListEnts.append("Walkdir : " + av.playerControl.getWalkDirection() + "\n");
							}
							if (pe.canMove()) { // Only bother with things that can move
								pe.calcPosition(this, serverTimePast); //pe.getWorldTranslation();
							}
						}
						if (e instanceof IProcessByClient) {
							IProcessByClient pbc = (IProcessByClient)e;
							pbc.process(this, tpf_secs);
						}
					}
					this.hud.log_ta.setText(strListEnts.toString());
					/*if (this.avatar != null) {
						avatar.process(this, tpf_secs);
					}*/
				}
			}

			loopTimer.waitForFinish(); // Keep clients and server running at same speed
			loopTimer.start();
		} catch (Exception ex) {
			ex.printStackTrace();
			this.quit();
		}
	}


	protected abstract IEntity createEntity(NewEntityMessage msg);
	
	
	private void storeAvatarPosition(long serverTime) {
		// Store our position
		EntityPositionData epd = new EntityPositionData();
		epd.serverTimestamp = serverTime;
		epd.position = avatar.getWorldTranslation().clone();
		//epd.rotation not required
		this.clientAvatarPositionData.addPositionData(epd);

	}


	@Override
	public void messageReceived(MyAbstractMessage message) {
		if (Settings.DEBUG_MSGS) {
			Settings.p("Rcvd " + message.getClass().getSimpleName());
		}
		
		if (message instanceof PingMessage) {
			PingMessage pingMessage = (PingMessage) message;
			if (!pingMessage.s2c) {
				long p2 = System.currentTimeMillis() - pingMessage.originalSentTime;
				this.pingRTT = this.pingCalc.add(p2);
				clientToServerDiffTime = pingMessage.responseSentTime - pingMessage.originalSentTime - (pingRTT/2); // If running on the same server, this should be 0! (or close enough)
				//Settings.p("pingRTT = " + pingRTT);
				//Settings.p("clientToServerDiffTime = " + clientToServerDiffTime);

			} else {
				pingMessage.responseSentTime = System.currentTimeMillis();
				networkClient.sendMessageToServer(message); // Send it straight back
			}

		} else if (message instanceof GameSuccessfullyJoinedMessage) {
			GameSuccessfullyJoinedMessage npcm = (GameSuccessfullyJoinedMessage)message;
			if (this.playerID <= 0) {
				this.playerID = npcm.playerID;
				this.playersAvatarID = npcm.avatarEntityID;
				this.hud.setPlayerID(this.playerID);

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
			synchronized (unprocessedMessages) {
				unprocessedMessages.add(newEntityMessage);
			}

		} else if (message instanceof EntityUpdateMessage) {
			EntityUpdateMessage eum = (EntityUpdateMessage)message;
			synchronized (unprocessedMessages) {
				unprocessedMessages.add(eum);
			}

		} else if (message instanceof RemoveEntityMessage) {
			RemoveEntityMessage rem = (RemoveEntityMessage)message;
			synchronized (unprocessedMessages) {
				unprocessedMessages.add(rem);
			}

		} else if (message instanceof GeneralCommandMessage) {
			GeneralCommandMessage rem = (GeneralCommandMessage)message;
			synchronized (unprocessedMessages) {
				unprocessedMessages.add(rem);
			}
		} else if (message instanceof WelcomeClientMessage) {
			WelcomeClientMessage rem = (WelcomeClientMessage)message;
			acceptByServer = true; // Need to wait until we receive something from the server before we can send to them
			networkClient.sendMessageToServer(new NewPlayerRequestMessage("Mark Gray", 1));
			
		} else {
			throw new RuntimeException("Unknown message type: " + message);
		}

	}


	@Override
	public void collision(PhysicsCollisionEvent event) {
		String s = event.getObjectA().getUserObject().toString() + " collided with " + event.getObjectB().getUserObject().toString();
		//System.out.println(s);

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
			quit();
		} else if (name.equalsIgnoreCase(TEST)) {
			this.avatar.playerControl.warp(new Vector3f(10, 10, 10));
		}
	}


	private void quit() {
		Settings.p("quit()");
		if (playerID >= 0) {
			this.networkClient.sendMessageToServer(new PlayerLeftMessage(this.playerID));
			/*try {
				executor.awaitTermination(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
		}
		this.networkClient.close();
		this.stop();

	}


	@Override
	public boolean isServer() {
		return false;
	}


	/*	@Override
	public void physicsTick(PhysicsSpace arg0, float arg1) {

	}


	@Override
	public void prePhysicsTick(PhysicsSpace arg0, float arg1) {
		if (avatar != null) {
			//this.avatar.resetWalkDir();
		}
	}

	 */
	@Override
	public Type getJmeContext() {
		return getContext().getType();
	}

}
