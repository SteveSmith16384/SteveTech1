package com.scs.stevetech1.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.font.BitmapFont;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.system.AppSettings;
import com.scs.simplephysics.ICollisionListener;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IAnimated;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IPreprocess;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.hud.HUD;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.input.MouseAndKeyboardCamera;
import com.scs.stevetech1.netmessages.AbilityUpdateMessage;
import com.scs.stevetech1.netmessages.AvatarStatusMessage;
import com.scs.stevetech1.netmessages.EntityUpdateMessage;
import com.scs.stevetech1.netmessages.GameStatusMessage;
import com.scs.stevetech1.netmessages.GameSuccessfullyJoinedMessage;
import com.scs.stevetech1.netmessages.GeneralCommandMessage;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.netmessages.NewEntityMessage;
import com.scs.stevetech1.netmessages.NewPlayerRequestMessage;
import com.scs.stevetech1.netmessages.PingMessage;
import com.scs.stevetech1.netmessages.PlayerInputMessage;
import com.scs.stevetech1.netmessages.PlayerLeftMessage;
import com.scs.stevetech1.netmessages.RemoveEntityMessage;
import com.scs.stevetech1.netmessages.WelcomeClientMessage;
import com.scs.stevetech1.networking.IGameMessageClient;
import com.scs.stevetech1.networking.IMessageClientListener;
import com.scs.stevetech1.networking.KryonetGameClient;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.AbstractClientEntityCreator;
import com.scs.stevetech1.shared.AbstractGameController;
import com.scs.stevetech1.shared.AverageNumberCalculator;
import com.scs.stevetech1.shared.HistoricalAnimationData;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.systems.AnimationSystem;

import ssmith.util.RealtimeInterval;

public abstract class AbstractGameClient extends AbstractGameController implements IEntityController, ActionListener, IMessageClientListener, ICollisionListener<PhysicalEntity> { 

	private static final String QUIT = "Quit";
	private static final String TEST = "Test";

	// Statuses
	public static final int STATUS_NOT_CONNECTED = 0;
	public static final int STATUS_CONNECTED = 1;
	public static final int STATUS_RCVD_WELCOME = 2;
	public static final int STATUS_SENT_JOIN_REQUEST = 3;
	public static final int STATUS_JOINED_GAME = 4;
	public static final int STATUS_GAME_STARTED = 5; // Have received all entities

	private HashMap<Integer, IEntity> clientOnlyEntities = new HashMap<>(100);

	public static BitmapFont guiFont_small;
	public static AppSettings settings;
	public IGameMessageClient networkClient;
	public HUD hud;
	public IInputDevice input;

	public AbstractClientAvatar currentAvatar;
	public int playerID = -1;
	public int side = -1;
	private AverageNumberCalculator pingCalc = new AverageNumberCalculator();
	public long pingRTT;
	public long clientToServerDiffTime; // Add to current time to get server time
	public int clientStatus = STATUS_NOT_CONNECTED;
	public SimpleGameData gameData;

	private RealtimeInterval sendInputsInterval = new RealtimeInterval(Globals.SERVER_TICKRATE_MS);
	private RealtimeInterval showGameTimeInterval = new RealtimeInterval(1000);
	private List<MyAbstractMessage> unprocessedMessages = new LinkedList<>();

	public long serverTime, renderTime;
	private String serverIP;
	private int port;

	// Entity systems
	//private UpdateAmmoCacheSystem updateAmmoSystem;
	private AnimationSystem animSystem;
	private AbstractClientEntityCreator entityCreator;

	protected AbstractGameClient(String _serverIP, int _port, AbstractClientEntityCreator _entityCreator) {
		super();

		serverIP = _serverIP;
		port = _port;
		this.entityCreator =_entityCreator;
		physicsController = new SimplePhysicsController<PhysicalEntity>(this);
		//updateAmmoSystem = new UpdateAmmoCacheSystem(this);
		animSystem = new AnimationSystem(this);

	}


	@Override
	public void simpleInitApp() {
		// Clear existing mappings
		getInputManager().clearMappings();
		getInputManager().clearRawInputListeners();

		assetManager.registerLocator("assets/", FileLocator.class); // default
		assetManager.registerLocator("assets/", ClasspathLocator.class);

		guiFont_small = getAssetManager().loadFont("Interface/Fonts/Console.fnt");

		cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.01f, Globals.CAM_DIST);

		getInputManager().addMapping(QUIT, new KeyTrigger(KeyInput.KEY_ESCAPE));
		getInputManager().addListener(this, QUIT);            

		getInputManager().addMapping(TEST, new KeyTrigger(KeyInput.KEY_T));
		getInputManager().addListener(this, TEST);            

		setUpLight();

		//this.rootNode.attachChild(JMEFunctions.GetGrid(assetManager, 10));

		hud = this.createHUD(getCamera());
		input = new MouseAndKeyboardCamera(getCamera(), getInputManager());

		if (Globals.RECORD_VID) {
			Globals.p("Recording video");
			VideoRecorderAppState video_recorder = new VideoRecorderAppState();
			stateManager.attach(video_recorder);
		}

		// Don't connect to network until JME is up and running!
		try {
			networkClient = new KryonetGameClient(serverIP, port, port, this); // todo - connect to lobby first!
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


	protected void setUpLight() {
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(.5f));
		getRootNode().addLight(al);

		DirectionalLight sun = new DirectionalLight();
		sun.setColor(ColorRGBA.Yellow);
		sun.setDirection(new Vector3f(.5f, -1f, .5f).normalizeLocal());
		rootNode.addLight(sun);
	}


	@Override
	public void simpleUpdate(float tpf_secs) {
		try {
			serverTime = System.currentTimeMillis() + this.clientToServerDiffTime;

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
									this.actuallyAddEntity(e); // Need to add it immediately so there's an avatar to add the grenade launcher to
								}
							} else {
								// We already know about it
							}

						} else if (message instanceof EntityUpdateMessage) {
							if (clientStatus >= STATUS_JOINED_GAME) {
								EntityUpdateMessage mainmsg = (EntityUpdateMessage)message;
								for(EntityUpdateMessage.UpdateData eum : mainmsg.data) {
									IEntity e = this.entities.get(eum.entityID);
									if (e != null) {
										//Settings.p("Received EntityUpdateMessage for " + e);
										//EntityPositionData epd = new EntityPositionData(eum.pos, eum.dir, mainmsg.timestamp);
										PhysicalEntity pe = (PhysicalEntity)e;
										if (eum.force) {
											// Set it now!
											pe.setWorldTranslation(eum.pos);
											pe.setWorldRotation(eum.dir);
											pe.clearPositiondata();
											if (pe == this.currentAvatar) {
												currentAvatar.clientAvatarPositionData.clear(); // Clear our local data as well
												currentAvatar.storeAvatarPosition(serverTime);
												// Stop us walking!
												//this.avatar.resetWalkDir();
											}
										}
										pe.addPositionData(eum.pos, eum.dir, mainmsg.timestamp); // Store the position for use later
										if (pe instanceof IAnimated && eum.animation != null && eum.animation.length() > 0) {
											IAnimated ia = (IAnimated)pe;
											ia.getAnimList().addData(new HistoricalAnimationData(mainmsg.timestamp, eum.animation));
										}
									} else {
										Globals.p("Unknown entity ID: " + eum.entityID);
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
								clientStatus = STATUS_GAME_STARTED;
							}

						} else if (message instanceof AbilityUpdateMessage) {
							AbilityUpdateMessage aum = (AbilityUpdateMessage) message;
							IAbility a = (IAbility)entities.get(aum.entityID);
							if (a != null) {
								if (aum.timestamp > a.getLastUpdateTime()) {
									a.decode(aum);
									a.setLastUpdateTime(aum.timestamp);
								}
							}
						} else if (message instanceof AvatarStatusMessage) {
							AvatarStatusMessage asm = (AvatarStatusMessage) message;
							AbstractAvatar avatar = (AbstractAvatar)this.entities.get(asm.entityID);
							if (avatar.alive != asm.alive) {
								// todo - show message or something
							}
							avatar.alive = asm.alive;							
						} else {
							throw new RuntimeException("Unknown message type: " + message);
						}
					}
				}

				if (clientStatus >= STATUS_CONNECTED && sendPingInterval.hitInterval()) {
					networkClient.sendMessageToServer(new PingMessage(false));
				}

				if (clientStatus == STATUS_GAME_STARTED) {

					if (this.currentAvatar != null) {
						// Send inputs
						if (sendInputsInterval.hitInterval()) {
							if (networkClient.isConnected()) {
								this.networkClient.sendMessageToServer(new PlayerInputMessage(this.input));
							}
						}
					}

					renderTime = serverTime - Globals.CLIENT_RENDER_DELAY; // Render from history

					if (Globals.SHOW_LATEST_AVATAR_POS_DATA_TIMESTAMP) {
						try {
							long timeDiff = this.currentAvatar.serverPositionData.getMostRecent().serverTimestamp - renderTime;
							this.hud.setDebugText("Latest Data is " + timeDiff + " newer than we need");
						} catch (Exception ex) {
							// do nothing, no data yet
						}
					}


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
/*
						if (e instanceof IRequiresAmmoCache) {
							updateAmmoSystem.process((IRequiresAmmoCache)e, tpf_secs);
						}
*/
						if (e instanceof PhysicalEntity) {
							PhysicalEntity pe = (PhysicalEntity)e;  // pe.getWorldTranslation();
							if (pe.moves) { // Only bother with things that can move
								pe.calcPosition(this, renderTime, tpf_secs); // Must be before we process physics as this calcs additionalForce
							}
							strListEnts.append(pe.name + ": " + pe.getWorldTranslation() + "\n");
						}

						if (e instanceof IProcessByClient) {
							IProcessByClient pbc = (IProcessByClient)e;
							pbc.processByClient(this, tpf_secs); // Mainly to process client-side movement of the avatar
						}

						if (e instanceof IAnimated) {
							IAnimated pbc = (IAnimated)e;
							this.animSystem.process(pbc, tpf_secs);
						}
					}

					for (IEntity e : this.clientOnlyEntities.values()) {
						if (e instanceof IProcessByClient) {
							IProcessByClient pbc = (IProcessByClient)e;
							pbc.processByClient(this, tpf_secs);
						}
					}

					this.hud.log_ta.setText(strListEnts.toString());
				}
			}

			if (showGameTimeInterval.hitInterval()) {
				if (this.gameData != null) {
					this.hud.setGameStatus(SimpleGameData.getStatusDesc(gameData.getGameStatus()));
					this.hud.setGameTime(this.gameData.getTime(serverTime));

				}
			}
			loopTimer.waitForFinish(); // Keep clients and server running at same speed
			loopTimer.start();

		} catch (Exception ex) {
			ex.printStackTrace();
			this.quit("Error: " + ex);
		}
	}


	/*
	 * For when a client requests the server to create an entity, e.g. a grenade (for lobbing).
	 */
	protected final IEntity createEntity(NewEntityMessage msg) {
		return this.entityCreator.createEntity(this, msg);
	}


	@Override
	public void messageReceived(MyAbstractMessage message) {
		if (Globals.DEBUG_MSGS) {
			Globals.p("Rcvd " + message.getClass().getSimpleName());
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
				//this.hud.setDebugText("PlayerID=" + this.playerID);
				//Settings.p("We are player " + playerID);
				clientStatus = STATUS_JOINED_GAME;
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
			if (clientStatus < STATUS_RCVD_WELCOME) {
				clientStatus = STATUS_RCVD_WELCOME; // Need to wait until we receive something from the server before we can send to them?
				networkClient.sendMessageToServer(new NewPlayerRequestMessage("Mark Gray", 1));
				clientStatus = STATUS_SENT_JOIN_REQUEST;
			} else {
				throw new RuntimeException("Received second welcome message");
			}

		} else if (message instanceof AbilityUpdateMessage) {
			AbilityUpdateMessage aum = (AbilityUpdateMessage)message;
			unprocessedMessages.add(aum);

		} else if (message instanceof GameStatusMessage) {
			GameStatusMessage gsm = (GameStatusMessage)message;
			this.gameData = gsm.gameData;

		} else {
			throw new RuntimeException("Unknown message type: " + message);
		}

		if (Globals.DEBUG_MSGS) {
			if (clientStatus < STATUS_RCVD_WELCOME) {
				Globals.p("Still not received Welcome message");
			}
		}

	}


	@Override
	public void addEntity(IEntity e) {
		if (e.getID() <= 0) {
			throw new RuntimeException("No entity id!");
		}
		this.toAdd.add(e);
	}


	private void actuallyAddEntity(IEntity e) {
		synchronized (entities) {
			if (e.getID() <= 0) {
				throw new RuntimeException("No entity id!");
			}
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
				if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
					Globals.p("Removing " + e.getName());
				}
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
			if (value) {
				quit("User chose to");
			}
		} else if (name.equalsIgnoreCase(TEST)) {
			if (value) {
				//this.avatar.setWorldTranslation(new Vector3f(10, 10, 10));

				// Toggle client sync
				Globals.SYNC_AVATAR_POS = !Globals.SYNC_AVATAR_POS;
				Globals.p("Client sync is " + Globals.SYNC_AVATAR_POS);
			}
		}
	}


	private void quit(String reason) {
		Globals.p("quitting: " + reason);
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

	/*
	@Override
	public Type getJmeContext() {
		return getContext().getType();
	}
	 */

	@Override
	public void connected() {
		Globals.p("Connected!");

	}


	@Override
	public void disconnected() {
		Globals.p("Disconnected!");
		quit("");
	}


	@Override
	public SimplePhysicsController<PhysicalEntity> getPhysicsController() {
		return physicsController;
	}

	/*
	@Override
	public boolean canCollide(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		return true;
	}
	 */

	@Override
	public void collisionOccurred(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b, Vector3f point) {
		/*if (a.userObject instanceof Floor == false && b.userObject instanceof Floor == false) {
			Globals.p("Collision between " + a.userObject + " and " + b.userObject);
		}*/
	}


	//@Override
	public void addClientOnlyEntity(IEntity e) {
		this.clientOnlyEntities.put(e.getID(), e); // todo - create toAdd
	}


	public void removeClientOnlyEntity(IEntity e) {
		this.clientOnlyEntities.remove(e.getID());
	}


	@Override
	public int getNextEntityID() {
		return nextEntityID.getAndAdd(1);
	}


}
