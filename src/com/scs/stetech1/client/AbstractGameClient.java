package com.scs.stetech1.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.asset.plugins.FileLocator;
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
import com.scs.simplephysics.ICollisionListener;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.components.IPreprocess;
import com.scs.stetech1.components.IProcessByClient;
import com.scs.stetech1.components.IRequiresAmmoCache;
import com.scs.stetech1.data.GameData;
import com.scs.stetech1.entities.ClientPlayersAvatar;
import com.scs.stetech1.entities.PhysicalEntity;
import com.scs.stetech1.hud.HUD;
import com.scs.stetech1.input.IInputDevice;
import com.scs.stetech1.input.MouseAndKeyboardCamera;
import com.scs.stetech1.netmessages.AbilityUpdateMessage;
import com.scs.stetech1.netmessages.EntityUpdateMessage;
import com.scs.stetech1.netmessages.GameStatusMessage;
import com.scs.stetech1.netmessages.GameSuccessfullyJoinedMessage;
import com.scs.stetech1.netmessages.GeneralCommandMessage;
import com.scs.stetech1.netmessages.MyAbstractMessage;
import com.scs.stetech1.netmessages.NewEntityMessage;
import com.scs.stetech1.netmessages.NewPlayerRequestMessage;
import com.scs.stetech1.netmessages.PingMessage;
import com.scs.stetech1.netmessages.PlayerInputMessage;
import com.scs.stetech1.netmessages.PlayerLeftMessage;
import com.scs.stetech1.netmessages.RemoveEntityMessage;
import com.scs.stetech1.netmessages.WelcomeClientMessage;
import com.scs.stetech1.networking.IMessageClient;
import com.scs.stetech1.networking.IMessageClientListener;
import com.scs.stetech1.networking.KryonetClient;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.AverageNumberCalculator;
import com.scs.stetech1.shared.EntityPositionData;
import com.scs.stetech1.shared.IAbility;
import com.scs.stetech1.shared.IEntityController;
import com.scs.stetech1.systems.UpdateAmmoCacheSystem;
import com.scs.testgame.entities.Floor;

import ssmith.util.FixedLoopTime;
import ssmith.util.RealtimeInterval;

public abstract class AbstractGameClient extends SimpleApplication implements IEntityController, ActionListener, IMessageClientListener, ICollisionListener<PhysicalEntity> { 

	private static final String QUIT = "Quit";
	private static final String TEST = "Test";

	// Statuses
	public static final int STATUS_NOT_CONNECTED = 0;
	public static final int STATUS_CONNECTED = 1;
	public static final int STATUS_RCVD_WELCOME = 2;
	public static final int STATUS_SENT_JOIN_REQUEST = 3;
	public static final int STATUS_JOINED_GAME = 4;
	public static final int STATUS_GAME_STARTED = 5; // Have received all entities

	public HashMap<Integer, IEntity> entities = new HashMap<>(100);
	private LinkedList<IEntity> toAdd = new LinkedList<IEntity>();
	private LinkedList<Integer> toRemove = new LinkedList<Integer>(); 

	private RealtimeInterval sendPingInt = new RealtimeInterval(Settings.PING_INTERVAL_MS);
	public static BitmapFont guiFont_small;
	public static AppSettings settings;
	public IMessageClient networkClient;
	public HUD hud;
	public IInputDevice input;

	public ClientPlayersAvatar avatar;
	public int playerID = -1;
	public int side = -1;
	private AverageNumberCalculator pingCalc = new AverageNumberCalculator();
	public long pingRTT;
	public long clientToServerDiffTime; // Add to current time to get server time
	public int status = STATUS_NOT_CONNECTED;

	private RealtimeInterval sendInputsInterval = new RealtimeInterval(Settings.SERVER_TICKRATE_MS);
	private FixedLoopTime loopTimer = new FixedLoopTime(Settings.SERVER_TICKRATE_MS);
	private List<MyAbstractMessage> unprocessedMessages = new LinkedList<>();
	private SimplePhysicsController<PhysicalEntity> physicsController;

	private UpdateAmmoCacheSystem updateAmmoSystem;

	protected AbstractGameClient() {
		super();

		physicsController = new SimplePhysicsController<PhysicalEntity>(this);
		updateAmmoSystem = new UpdateAmmoCacheSystem(this);
	}


	@Override
	public void simpleInitApp() {
		// Clear existing mappings
		getInputManager().clearMappings();
		getInputManager().clearRawInputListeners();

		assetManager.registerLocator("assets/", FileLocator.class); // default
		assetManager.registerLocator("assets/", ClasspathLocator.class);

		guiFont_small = getAssetManager().loadFont("Interface/Fonts/Console.fnt");

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
			networkClient = new KryonetClient(this);// SpiderMonkeyClient(this);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}

		loopTimer.start();

	}


	private HUD createHUD(Camera c) {
		BitmapFont guiFont_small = getAssetManager().loadFont("Interface/Fonts/Console.fnt");
		HUD hud = new HUD(this, guiFont_small, c);
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
		try {
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
								if (e != null) {
									//this.addEntity(e);
									this.actuallyAddEntity(e); // Need to add it immediately so there's an avatar to add the grenade launche to
								}
							} else {
								// We already know about it
							}

						} else if (message instanceof EntityUpdateMessage) {
							if (status >= STATUS_JOINED_GAME) {
								EntityUpdateMessage mainmsg = (EntityUpdateMessage)message;
								for(EntityUpdateMessage.UpdateData eum : mainmsg.data) {
									IEntity e = this.entities.get(eum.entityID);
									if (e != null) {
										//Settings.p("Received EntityUpdateMessage for " + e);
										EntityPositionData epd = new EntityPositionData();
										epd.serverTimestamp = mainmsg.timestamp;
										epd.rotation = eum.dir;
										epd.position = eum.pos;

										PhysicalEntity pe = (PhysicalEntity)e;
										if (eum.force) {
											// Set it now!
											pe.setWorldTranslation(epd.position);
											pe.setWorldRotation(epd.rotation);
											pe.clearPositiondata();
											if (pe == this.avatar) {
												avatar.clientAvatarPositionData.clearPositiondata(); // Clear our local data as well
												avatar.storeAvatarPosition(serverTime);
												// Stop us walking!
												//this.avatar.resetWalkDir();
											}
										}
										pe.addPositionData(epd); // Store the position for use later
										//Settings.p("New position for " + e + ": " + eum.pos);
									} else {
										Settings.p("Unknown entity ID: " + eum.entityID);
										// Ask the server for entity details since we don't know about it.
										// No, since we might not have joined the game yet! (server uses broadcast()
										// networkClient.sendMessageToServer(new UnknownEntityMessage(eum.entityID));
									}
								}
							}
						} else if (message instanceof RemoveEntityMessage) {
							RemoveEntityMessage rem = (RemoveEntityMessage)message;
							this.removeEntity(rem.entityID);

						} else if (message instanceof GeneralCommandMessage) { // We now have enough data to start
							GeneralCommandMessage msg = (GeneralCommandMessage)message;
							if (msg.command == GeneralCommandMessage.Command.AllEntitiesSent) {
								status = STATUS_GAME_STARTED;
							}

						} else if (message instanceof AbilityUpdateMessage) {
							AbilityUpdateMessage aum = (AbilityUpdateMessage)message;
							IAbility a = (IAbility)entities.get(aum.entityID);
							/*if (a == null) {
								throw new RuntimeException("todo");
							}*/
							if (aum.timestamp > a.getLastUpdateTime()) {
								a.decode(aum);
								a.setLastUpdateTime(aum.timestamp);
							}

						} else {
							throw new RuntimeException("Unknown message type: " + message);
						}
					}
				}

				if (status >= STATUS_CONNECTED && sendPingInt.hitInterval()) {
					networkClient.sendMessageToServer(new PingMessage(false));
				}

				if (status == STATUS_GAME_STARTED) {

					if (this.avatar != null) {
						// Send inputs
						if (sendInputsInterval.hitInterval()) {
							if (networkClient.isConnected()) {
								this.networkClient.sendMessageToServer(new PlayerInputMessage(this.input));
							}
						}
					}

					long serverTimePast = serverTime - Settings.CLIENT_RENDER_DELAY; // Render from history

					// Loop through each entity and process them				
					StringBuffer strListEnts = new StringBuffer(); // Log entities

					// Add and remove entities
					for(IEntity e : this.toAdd) {
						this.actuallyAddEntity(e);
					}
					this.toAdd.clear();

					for(Integer i : this.toRemove) {
						this.actuallyRemoveEntity(i);
					}
					this.toRemove.clear();

					for (IEntity e : this.entities.values()) {
						if (e instanceof IPreprocess) {
							IPreprocess p = (IPreprocess)e;
							p.preprocess();
						}

						if (e instanceof IRequiresAmmoCache) {
							updateAmmoSystem.process(e, tpf_secs);
						}
						
						if (e instanceof PhysicalEntity) {
							PhysicalEntity pe = (PhysicalEntity)e;
							if (pe.canMove()) { // Only bother with things that can move
								pe.calcPosition(this, serverTimePast); // Must be before we process physics as this calcs additionalForce
							}
							strListEnts.append(pe.name + ": " + pe.getWorldTranslation() + "\n");
						}
						if (e instanceof IProcessByClient) {
							IProcessByClient pbc = (IProcessByClient)e;
							pbc.processByClient(this, tpf_secs); // Mainly to process client-side movement of the avatar
						}
					}

					this.hud.log_ta.setText(strListEnts.toString());
				}
			}

			loopTimer.waitForFinish(); // Keep clients and server running at same speed
			loopTimer.start();
			
		} catch (Exception ex) {
			ex.printStackTrace();
			this.quit("Error: " + ex);
		}
	}


	protected abstract IEntity createEntity(NewEntityMessage msg);


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
				this.side = npcm.side;
				this.hud.setPlayerID(this.playerID);
				Settings.p("We are player " + playerID);
				status = STATUS_JOINED_GAME;
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
			if (status < STATUS_RCVD_WELCOME) {
				status = STATUS_RCVD_WELCOME; // Need to wait until we receive something from the server before we can send to them?
				networkClient.sendMessageToServer(new NewPlayerRequestMessage("Mark Gray", 1));
				status = STATUS_SENT_JOIN_REQUEST;
			} else {
				throw new RuntimeException("Received second welcome message");
			}

		} else if (message instanceof AbilityUpdateMessage) {
			AbilityUpdateMessage aum = (AbilityUpdateMessage)message;
			unprocessedMessages.add(aum);

		} else if (message instanceof GameStatusMessage) {
			GameStatusMessage gsm = (GameStatusMessage)message;
			this.hud.setGameStatus(GameData.getStatusDesc(gsm.gameStatus));
			this.hud.setGameTime(""+gsm.gameTimeMS/1000);

		} else {
			throw new RuntimeException("Unknown message type: " + message);
		}

		if (Settings.DEBUG_MSGS) {
			if (status < STATUS_RCVD_WELCOME) {
				Settings.p("Still not received Welcome message");
			}
		}

	}


	@Override
	public void addEntity(IEntity e) {
		this.toAdd.add(e);
	}


	private void actuallyAddEntity(IEntity e) {
		synchronized (entities) {
			this.entities.put(e.getID(), e);
		}
	}


	@Override
	public void removeEntity(int id) {
		this.toRemove.add(id);
	}


	private void actuallyRemoveEntity(int id) {
		synchronized (entities) {
			IEntity e = this.entities.get(id);
			if (e != null) {
				Settings.p("Removing " + e.getName());
				if (e instanceof PhysicalEntity) {
					PhysicalEntity pe =(PhysicalEntity)e;
					if (pe.simpleRigidBody != null) {
						this.physicsController.removeSimpleRigidBody(pe.simpleRigidBody);
					}
					pe.getMainNode().removeFromParent();
				}
				this.entities.remove(id);
			}
		}
	}


	@Override
	public void onAction(String name, boolean value, float tpf) {
		if (name.equalsIgnoreCase(QUIT)) {
			quit("User chose to");
		} else if (name.equalsIgnoreCase(TEST)) {
			//this.avatar.setWorldTranslation(new Vector3f(10, 10, 10));
			Settings.SYNC_CLIENT_POS = !Settings.SYNC_CLIENT_POS;
			this.hud.log_ta.addLine("Client sync is " + Settings.SYNC_CLIENT_POS);
		}
	}


	private void quit(String reason) {
		Settings.p("quitting: " + reason);
		if (this.networkClient.isConnected()) {
			if (playerID >= 0) {
				this.networkClient.sendMessageToServer(new PlayerLeftMessage(this.playerID));
				/*try {
				executor.awaitTermination(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
			}
			this.networkClient.close();
		}
		this.stop();

	}


	@Override
	public boolean isServer() {
		return false;
	}


	@Override
	public Type getJmeContext() {
		return getContext().getType();
	}


	@Override
	public void connected() {
		Settings.p("Connected!");

	}


	@Override
	public void disconnected() {
		Settings.p("Disconnected!");
		quit("");
	}


	@Override
	public SimplePhysicsController<PhysicalEntity> getPhysicsController() {
		return physicsController;
	}


	@Override
	public boolean canCollide(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		return true;
	}


	@Override
	public void collisionOccurred(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b, Vector3f point) {
		if (a.userObject instanceof Floor == false && b.userObject instanceof Floor == false) {
			Settings.p("Collision between " + a.userObject + " and " + b.userObject);
		}

	}


}
