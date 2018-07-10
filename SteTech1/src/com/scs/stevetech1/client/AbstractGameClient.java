package com.scs.stevetech1.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.BackingStoreException;

import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.bounding.BoundingBox;
import com.jme3.input.FlyByCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Caps;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.scs.simplephysics.ICollisionListener;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IAnimatedClientSide;
import com.scs.stevetech1.components.IClientControlled;
import com.scs.stevetech1.components.IDrawOnHUD;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IKillable;
import com.scs.stevetech1.components.ILaunchable;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.components.IPlayerControlled;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.data.SimplePlayerData;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.entities.ExplosionShard;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.hud.IHUD;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.input.MouseAndKeyboardCamera;
import com.scs.stevetech1.netmessages.AbilityUpdateMessage;
import com.scs.stevetech1.netmessages.AvatarStartedMessage;
import com.scs.stevetech1.netmessages.AvatarStatusMessage;
import com.scs.stevetech1.netmessages.EntityKilledMessage;
import com.scs.stevetech1.netmessages.EntityLaunchedMessage;
import com.scs.stevetech1.netmessages.EntityUpdateData;
import com.scs.stevetech1.netmessages.EntityUpdateMessage;
import com.scs.stevetech1.netmessages.GameLogMessage;
import com.scs.stevetech1.netmessages.GameOverMessage;
import com.scs.stevetech1.netmessages.GameSuccessfullyJoinedMessage;
import com.scs.stevetech1.netmessages.GeneralCommandMessage;
import com.scs.stevetech1.netmessages.GunReloadingMessage;
import com.scs.stevetech1.netmessages.JoinGameFailedMessage;
import com.scs.stevetech1.netmessages.ModelBoundsMessage;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.netmessages.NewEntityData;
import com.scs.stevetech1.netmessages.NewEntityMessage;
import com.scs.stevetech1.netmessages.NewPlayerRequestMessage;
import com.scs.stevetech1.netmessages.PingMessage;
import com.scs.stevetech1.netmessages.PlaySoundMessage;
import com.scs.stevetech1.netmessages.PlayerInputMessage;
import com.scs.stevetech1.netmessages.PlayerLeftMessage;
import com.scs.stevetech1.netmessages.RemoveEntityMessage;
import com.scs.stevetech1.netmessages.SetAvatarMessage;
import com.scs.stevetech1.netmessages.ShowMessage;
import com.scs.stevetech1.netmessages.SimpleGameDataMessage;
import com.scs.stevetech1.netmessages.WelcomeClientMessage;
import com.scs.stevetech1.netmessages.lobby.ListOfGameServersMessage;
import com.scs.stevetech1.networking.IGameMessageClient;
import com.scs.stevetech1.networking.IMessageClientListener;
import com.scs.stevetech1.networking.KryonetGameClient;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.systems.client.AnimationSystem;
import com.scs.stevetech1.systems.client.ClientEntityLauncherSystem;

import ssmith.lang.NumberFunctions;
import ssmith.util.AverageNumberCalculator;
import ssmith.util.ConsoleInputListener;
import ssmith.util.FixedLoopTime;
import ssmith.util.RealtimeInterval;
import ssmith.util.TextConsole;

public abstract class AbstractGameClient extends SimpleApplication implements IClientApp, IEntityController, 
ActionListener, IMessageClientListener, ICollisionListener<PhysicalEntity>, ConsoleInputListener { 

	// Statuses
	public static final int STATUS_NOT_CONNECTED = 0;
	//public static final int STATUS_CONNECTED_TO_LOBBY = 1;
	public static final int STATUS_CONNECTED_TO_GAME = 2;
	public static final int STATUS_RCVD_WELCOME = 3;
	public static final int STATUS_SENT_JOIN_REQUEST = 4;
	public static final int STATUS_JOINED_GAME = 5; // About to be sent all the entities
	public static final int STATUS_ENTS_RCVD_NOT_ADDED = 6; // Have received all entities, but not added them yet
	public static final int STATUS_IN_GAME = 7; // Have received all entities and added them

	private static final String JME_SETTINGS_NAME = "jme_client_settings.txt";

	// Global controls
	private static final String RELOAD = "Reload";
	private static final String QUIT = "Quit";
	protected static final String TEST = "Test";

	private static AtomicInteger nextEntityID = new AtomicInteger(-1);

	public HashMap<Integer, IEntity> entities = new HashMap<>(100);
	protected ArrayList<IEntity> entitiesForProcessing = new ArrayList<>(10); // Entites that we need to iterate over in game loop
	protected LinkedList<PhysicalEntity> entitiesToAddToGame = new LinkedList<PhysicalEntity>(); // Entities to add to RootNode, as we don't add them immed
	protected LinkedList<Integer> entitiesToRemove = new LinkedList<Integer>(); // Still have a list so we don't have to loop through ALL entities
	private ArrayList<IEntity> clientOnlyEntities = new ArrayList<>(100);

	protected SimplePhysicsController<PhysicalEntity> physicsController; // Checks all collisions
	protected FixedLoopTime loopTimer;  // Keep client and server running at the same time

	public int tickrateMillis, clientRenderDelayMillis, timeoutMillis;

	private RealtimeInterval sendPingInterval = new RealtimeInterval(Globals.PING_INTERVAL_MS);

	private String gameCode; // To check the right type of client is connecting
	private String playerName = "Player_" + NumberFunctions.rnd(1, 1000);
	//private KryonetLobbyClient lobbyClient;
	public IGameMessageClient networkClient;
	public IHUD hud;
	public IInputDevice input;

	// On-screen gun
	public Node playersWeaponNode;
	private Spatial weaponModel;
	private float finishedReloadAt;
	private boolean currentlyReloading = false;
	private float gunAngle = 0;

	public AbstractClientAvatar currentAvatar;
	public int currentAvatarID = -1; // In case the avatar physical entity gets replaced, we can re-assign it
	public int playerID = -1;
	public int side = -1;
	private AverageNumberCalculator pingCalc = new AverageNumberCalculator(4);
	public long pingRTT;
	private long clientToServerDiffTime; // Add to current time to get server time
	public int clientStatus = STATUS_NOT_CONNECTED;
	public SimpleGameData gameData;
	public ArrayList<SimplePlayerData> playersList;

	protected Node gameNode = new Node("GameNode");
	protected Node debugNode = new Node("DebugNode");
	protected FilterPostProcessor fpp;

	private List<MyAbstractMessage> unprocessedMessages = new LinkedList<>();

	public long serverTime, renderTime;
	private String gameServerIP;//, lobbyIP;
	private int gamePort;//, lobbyPort;
	private float mouseSens;
	private String key;

	// Subnodes
	private int nodeSize = Globals.SUBNODE_SIZE;
	public HashMap<String, Node> nodes;

	// Entity systems
	private AnimationSystem animSystem;
	private ClientEntityLauncherSystem launchSystem;

	/**
	 * 
	 * @param _gameCode
	 * @param _key
	 * @param appTitle
	 * @param logoImage
	 * @param _gameServerIP
	 * @param _gamePort
	 * @param _tickrateMillis
	 * @param _clientRenderDelayMillis
	 * @param _timeoutMillis
	 * @param _mouseSens
	 */
	protected AbstractGameClient(String _gameCode, String _key, String appTitle, String logoImage, String _gameServerIP, int _gamePort, //String _lobbyIP, int _lobbyPort, 
			int _tickrateMillis, int _clientRenderDelayMillis, int _timeoutMillis, float _mouseSens) { // float gravity, float aerodynamicness, 
		super();

		gameCode = _gameCode;
		key = _key;

		tickrateMillis = _tickrateMillis;
		clientRenderDelayMillis = _clientRenderDelayMillis;
		timeoutMillis = _timeoutMillis;

		loopTimer = new FixedLoopTime(tickrateMillis);

		gameServerIP = _gameServerIP;
		gamePort = _gamePort;
		//lobbyIP = _lobbyIP;
		//lobbyPort = _lobbyPort;

		physicsController = new SimplePhysicsController<PhysicalEntity>(this, Globals.SUBNODE_SIZE);
		animSystem = new AnimationSystem(this);
		launchSystem = new ClientEntityLauncherSystem(this);

		nodes = new HashMap<String, Node>();

		mouseSens = _mouseSens;

		settings = new AppSettings(true);
		try {
			settings.load(JME_SETTINGS_NAME);
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		settings.setUseJoysticks(true);

		//settings.setAudioRenderer(null); // Todo Avoid error with no soundcard
		//super.audioRenderer = null;

		settings.setTitle(Globals.HIDE_BELLS_WHISTLES ? "Client" : appTitle);// + " (v" + Settings.VERSION + ")");
		settings.setSettingsDialogImage(logoImage);

		setSettings(settings);
		setPauseOnLostFocus(false); // Needs to always be in sync with server!

		try {
			settings.save(JME_SETTINGS_NAME);
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}

		FlyCamAppState state = stateManager.getState(FlyCamAppState.class);
		if (state != null) {
			this.stateManager.detach(state);
		}

}


	@Override
	public void simpleInitApp() {
		// Clear existing mappings
		getInputManager().clearMappings();
		getInputManager().clearRawInputListeners();

		assetManager.registerLocator("assets/", FileLocator.class); // default
		assetManager.registerLocator("assets/", ClasspathLocator.class);

		cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.001f, Globals.CAM_VIEW_DIST);

		getInputManager().addMapping(RELOAD, new KeyTrigger(KeyInput.KEY_R));
		getInputManager().addListener(this, RELOAD);            

		getInputManager().addMapping(QUIT, new KeyTrigger(KeyInput.KEY_ESCAPE));
		getInputManager().addListener(this, QUIT);            

		getInputManager().addMapping(TEST, new KeyTrigger(KeyInput.KEY_T));
		getInputManager().addListener(this, TEST);            

		setUpLight();

		hud = this.createAndGetHUD();
		if (hud != null) {
			getGuiNode().attachChild(hud.getRootNode());
		}

		this.getRootNode().attachChild(this.debugNode);

		input = new MouseAndKeyboardCamera(getCamera(), getInputManager(), mouseSens);

		if (Globals.RECORD_VID) {
			Globals.p("Recording video");
			VideoRecorderAppState video_recorder = new VideoRecorderAppState();
			stateManager.attach(video_recorder);
		}
		
		// Don't connect to network until JME is up and running!
		/*try {
			if (lobbyIP != null) {
				lobbyClient = new KryonetLobbyClient(lobbyIP, lobbyPort, lobbyPort, this, timeoutMillis);
				this.clientStatus = STATUS_CONNECTED_TO_LOBBY;
				lobbyClient.sendMessageToServer(new RequestListOfGameServersMessage());
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}*/

		try {
			networkClient = new KryonetGameClient(gameServerIP, gamePort, gamePort, this, timeoutMillis, getListofMessageClasses());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}

		// Turn off stats
		setDisplayFps(false);
		setDisplayStatView(false);

		// Start console
		new TextConsole(this);

		//if (Globals.TOONISH) {
		this.setupFilters();
		//}

		loopTimer.start();

	}

	protected abstract Class[] getListofMessageClasses();

	protected abstract IHUD createAndGetHUD();

	public long getServerTime() {
		return System.currentTimeMillis() + clientToServerDiffTime;
	}


	/*
	 * Default light; override if required.
	 */
	protected void setUpLight() {
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White);
		getGameNode().addLight(al);

		DirectionalLight sun = new DirectionalLight();
		sun.setColor(ColorRGBA.Yellow);
		sun.setDirection(new Vector3f(.5f, -1f, .5f).normalizeLocal());
		getGameNode().addLight(sun);
	}


	@Override
	public void simpleUpdate(float tpf_secs) {
		if (tpf_secs > 1) {
			tpf_secs = 1;
		}
		
		try {
			serverTime = System.currentTimeMillis() + this.clientToServerDiffTime;  //this.entities
			renderTime = serverTime - clientRenderDelayMillis; // Render from history

			if (networkClient != null && networkClient.isConnected()) {

				// Process messages in JME thread
				synchronized (unprocessedMessages) {
					// Check we don't already know about it
					Iterator<MyAbstractMessage> mit = this.unprocessedMessages.iterator();
					while (mit.hasNext()) {
						MyAbstractMessage message = mit.next();// this.unprocessedMessages.remove(0);
						if (message.scheduled) {
							if (message.timestamp > renderTime) {
								continue;
							}
						}
						mit.remove();
						this.handleMessage(message);
					}
				}

				if (clientStatus >= STATUS_CONNECTED_TO_GAME && sendPingInterval.hitInterval()) {
					networkClient.sendMessageToServer(new PingMessage(false, 0));
				}

				// Remove entities
				while (this.entitiesToRemove.size() > 0) {
					int i = this.entitiesToRemove.getFirst();
					this.actuallyRemoveEntity(i);
				}

				// Add entities
				if (entitiesToAddToGame.size() > 0) {
					for (int i=0 ; i<entitiesToAddToGame.size() ; i++) {
						PhysicalEntity pe = entitiesToAddToGame.get(i);
						if (pe.timeToAdd < renderTime) {
							if (pe.getID() < 0 || this.entities.containsKey(pe.getID())) { // Check it is still in the game
								this.addEntityToGame(pe);
							}
							entitiesToAddToGame.remove(i);
							i--;
						}
					}
				} else {
					if (this.clientStatus == STATUS_ENTS_RCVD_NOT_ADDED) {
						this.getRootNode().attachChild(this.gameNode);
						this.showPlayersWeapon();
						clientStatus = STATUS_IN_GAME;
					}
				}

				if (currentlyReloading) {
					this.reloading(tpf_secs, this.finishedReloadAt > 0);
				}

				if (clientStatus == STATUS_IN_GAME) {

					this.sendInputs();

					if (Globals.SHOW_LATEST_AVATAR_POS_DATA_TIMESTAMP) {
						try {
							long timeDiff = this.currentAvatar.historicalPositionData.getMostRecent().serverTimestamp - renderTime;
							this.hud.setDebugText("Latest Data is " + timeDiff + " newer than we need");
						} catch (Exception ex) {
							// do nothing, no data yet
						}
					}

					// Systems
					this.launchSystem.process(renderTime); // this.entities

					if (Globals.STRICT) {
						for(IEntity e : this.entities.values()) {
							if (e.requiresProcessing()) {
								if (!this.entitiesForProcessing.contains(e)) {
									Globals.p("Warning: Processed entity " + e + " not in process list!");
								}
							}
						}
					}

					// Loop through each entity and process them
					//for (IEntity e : entitiesForProcessing.values()) { //entitiesForProcessing.size();
					for (int i=0 ; i<this.entitiesForProcessing.size() ; i++) {
						IEntity e = this.entitiesForProcessing.get(i); //this.rootNode;
						if (e.hasNotBeenRemoved()) {
							if (e instanceof IPlayerControlled) {
								IPlayerControlled p = (IPlayerControlled)e;
								p.resetPlayerInput();
							}
							if (e instanceof PhysicalEntity) {
								PhysicalEntity pe = (PhysicalEntity)e;

								pe.calcPosition(renderTime, tpf_secs); // Must be before we process physics as this calcs additionalForce
								pe.processChronoData(renderTime, tpf_secs);

								if (Globals.STRICT) {
									if (e instanceof AbstractClientAvatar == false && e instanceof ExplosionShard == false && e instanceof IClientControlled == false) {
										if (pe.simpleRigidBody != null) {
											if (pe.simpleRigidBody.movedByForces()) {
												Globals.p("Warning: client-side entity " + pe + " not kinematic!");
											}
										}
									}
								}

							}

							if (e instanceof IProcessByClient) {
								IProcessByClient pbc = (IProcessByClient)e;
								pbc.processByClient(this, tpf_secs); // Mainly to process client-side movement of the avatar
							}

							if (e instanceof IAnimatedClientSide) {
								IAnimatedClientSide pbc = (IAnimatedClientSide)e;
								this.animSystem.process(pbc, tpf_secs);
							}

							if (e instanceof IDrawOnHUD) {
								IDrawOnHUD doh = (IDrawOnHUD)e;
								doh.drawOnHud(cam);
							}
						}
					}

					// Show players gun
					if (playersWeaponNode != null) {
						playersWeaponNode.setLocalTranslation(cam.getLocation());
						playersWeaponNode.lookAt(cam.getLocation().add(cam.getDirection()), Vector3f.UNIT_Y);
					}

				}
			}

			loopTimer.waitForFinish(); // Keep clients and server running at same speed
			loopTimer.start();

		} catch (Exception ex) {
			Globals.HandleError(ex);
			this.quit("Error: " + ex);
		}
	}


	protected void handleMessage(MyAbstractMessage message) {
		if (message instanceof PingMessage) {
			PingMessage pingMessage = (PingMessage) message;
			if (!pingMessage.s2c) {
				long p2 = System.currentTimeMillis() - pingMessage.originalSentTime;
				this.pingRTT = this.pingCalc.add(p2);
				clientToServerDiffTime = pingMessage.responseSentTime - pingMessage.originalSentTime - (pingRTT/2); // If running on the same server, this should be 0! (or close enough)

			} else {
				pingMessage.responseSentTime = System.currentTimeMillis();
				networkClient.sendMessageToServer(message); // Send it straight back
			}

		} else if (message instanceof ShowMessage) {
			ShowMessage gsm = (ShowMessage)message;
			this.hud.showMessage(gsm.msg);

		} else if (message instanceof GameSuccessfullyJoinedMessage) {
			GameSuccessfullyJoinedMessage npcm = (GameSuccessfullyJoinedMessage)message;
			if (this.playerID <= 0) {
				this.playerID = npcm.playerID;
				this.side = npcm.side;
				//this.hud.setDebugText("PlayerID=" + this.playerID);
				clientStatus = STATUS_JOINED_GAME;
			} else {
				throw new RuntimeException("Already rcvd NewPlayerAckMessage");
			}

		} else if (message instanceof WelcomeClientMessage) {
			WelcomeClientMessage rem = (WelcomeClientMessage)message;
			if (clientStatus < STATUS_RCVD_WELCOME) {
				clientStatus = STATUS_RCVD_WELCOME; // Need to wait until we receive something from the server before we can send to them?
				networkClient.sendMessageToServer(new NewPlayerRequestMessage(gameCode, playerName, key));
				clientStatus = STATUS_SENT_JOIN_REQUEST;
			} else {
				throw new RuntimeException("Received second welcome message");
			}

		} else if (message instanceof SimpleGameDataMessage) {
			SimpleGameData oldGameData = this.gameData;
			SimpleGameDataMessage gsm = (SimpleGameDataMessage)message;
			this.gameData = gsm.gameData;
			if (oldGameData == null || oldGameData.gameID != gsm.gameData.gameID) {
				Globals.p("Client Game id is now " + gameData.gameID);
			}
			this.playersList = gsm.players;
			if (oldGameData == null) {
				this.gameStatusChanged(-1, this.gameData.getGameStatus());
			} else if (this.gameData.getGameStatus() != oldGameData.getGameStatus()) {
				this.gameStatusChanged(oldGameData.getGameStatus(), this.gameData.getGameStatus());
			}

		} else if (message instanceof NewEntityMessage) {
			NewEntityMessage newEntityMessage = (NewEntityMessage) message;
			if (this.gameData != null) {
				if (newEntityMessage.gameId == this.gameData.gameID) {
					for (NewEntityData data : newEntityMessage.data) {
						IEntity e = this.entities.get(data.entityID);
						if (e == null) {
							createEntity(data, newEntityMessage.timestamp);
						} else {
							// We already know about it. -  NO! Replace the entity!  NO NO! Don't replace it as the original has links to other entities!
							Globals.p("Ignoring new entity " + e + " as we already know about it");
						}
					}
				} else {
					Globals.p("Ignoring NewEntityMessage for game " + newEntityMessage.gameId);
					// It's not for this game, so ignore it
				}
			}

		} else if (message instanceof EntityUpdateMessage) {
			if (clientStatus >= STATUS_JOINED_GAME) {
				EntityUpdateMessage mainmsg = (EntityUpdateMessage)message;
				for(EntityUpdateData eud : mainmsg.data) {
					IEntity e = this.entities.get(eud.entityID);
					if (e != null) {
						if (Globals.DEBUG_NO_UPDATE_MSGS) {
							Globals.p("Received EntityUpdateMessage for " + e);
						}
						PhysicalEntity pe = (PhysicalEntity)e;
						pe.storePositionData(eud, mainmsg.timestamp);
						if (pe.chronoUpdateData != null) {
							pe.chronoUpdateData.addData(eud);
						}
						if (Globals.DEBUG_DIE_ANIM) {
							if (eud.animationCode == AbstractAvatar.ANIM_DIED) {
								Globals.p("Rcvd death anim for " + e);
							}
						}
					} else {
						// Globals.p("Unknown entity ID for update: " + eum.entityID);
						// Ask the server for entity details since we don't know about it.
						// No, since we might not have joined the game yet! (server uses broadcast()
						// networkClient.sendMessageToServer(new UnknownEntityMessage(eum.entityID));
					}
				}
			}

		} else if (message instanceof RemoveEntityMessage) {
			RemoveEntityMessage rem = (RemoveEntityMessage)message;
			if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
				Globals.p("Rcvd msg to remove entity " + rem.entityID);
			}
			IEntity e = this.entities.get(rem.entityID);
			if (e != null) {
				e.remove();
			} else {
				// See if it's in our list of entities waiting to be added
				/*boolean found = false;
				Iterator<IEntity> it = this.entitiesToAdd.iterator();
				while (it.hasNext()) {
					IEntity e2a = it.next();
					if (e2a.getID() == rem.entityID) {
						it.remove();
						found = true;
						break;
					}
				}*/
				//if (!found) {
				Globals.p("Ignoring msg to remove entity " + rem.entityID + " as we have no record of it");
				//}
			}

		} else if (message instanceof GeneralCommandMessage) {
			GeneralCommandMessage msg = (GeneralCommandMessage)message;
			//if (msg.gameID == this.getGameID()) { Might not have game id
			Globals.p("Rcvd GeneralCommandMessage: " + msg.command.toString());
			if (msg.command == GeneralCommandMessage.Command.AllEntitiesSent) { // We now have enough data to start
				clientStatus = STATUS_ENTS_RCVD_NOT_ADDED;
				this.hud.appendToLog("All entities received");
				allEntitiesSent();
			} else if (msg.command == GeneralCommandMessage.Command.RemoveAllEntities) { // We now have enough data to start
				this.removeAllEntities();
			} else if (msg.command == GeneralCommandMessage.Command.GameRestarting) {
				this.hud.appendToLog("Game restarting...");
				clientStatus = STATUS_JOINED_GAME;
			} else {
				throw new RuntimeException("Unknown command:" + msg.command);
			}

		} else if (message instanceof AbilityUpdateMessage) {
			AbilityUpdateMessage aum = (AbilityUpdateMessage) message;
			IAbility a = (IAbility)entities.get(aum.entityID);
			if (a != null) {
				if (aum.timestamp > a.getLastUpdateTime()) { // Is it the latest msg
					a.decode(aum);
					a.setLastUpdateTime(aum.timestamp);
				}
			}

		} else if (message instanceof EntityKilledMessage) {
			EntityKilledMessage asm = (EntityKilledMessage) message;
			PhysicalEntity killed = (PhysicalEntity)this.entities.get(asm.killedEntityID);
			PhysicalEntity killer = (PhysicalEntity)this.entities.get(asm.killerEntityID);
			if (killed.simpleRigidBody != null) {
				this.physicsController.removeSimpleRigidBody(killed.simpleRigidBody);
			}
			if (killer == this.currentAvatar) {
				Globals.p("You have killed " + killed);
			}
			if (killed instanceof IKillable) {
				IKillable ik = (IKillable)killed;
				ik.handleKilledOnClientSide(killer);
			}
			if (killed == this.currentAvatar) {
				this.currentAvatar.killer = killer;
			}

		} else if (message instanceof EntityLaunchedMessage) {
			/*if (Globals.DEBUG_SHOOTING) {
				Globals.p("Received EntityLaunchedMessage");
			}*/
			EntityLaunchedMessage elm = (EntityLaunchedMessage)message;
			if (elm.playerID != this.playerID) {
				this.launchSystem.scheduleLaunch(elm); //this.entities
			} else {
				// It was us that launched it in the first place!
				Globals.p("Ignoring entity launched message");
			}

		} else if (message instanceof AvatarStartedMessage) {
			if (Globals.DEBUG_PLAYER_RESTART) {
				Globals.p("Rcvd AvatarStartedMessage");
			}
			AvatarStartedMessage asm = (AvatarStartedMessage)message;
			if (this.currentAvatar != null && asm.entityID == this.currentAvatar.getID()) {
				AbstractAvatar avatar = (AbstractAvatar)this.entities.get(asm.entityID);
				avatar.setAlive(true);  // todo - NPE
				// Point camera fwds again
				cam.lookAt(cam.getLocation().add(Vector3f.UNIT_X), Vector3f.UNIT_Y);
				cam.update();

			}

		} else if (message instanceof ListOfGameServersMessage) {
			ListOfGameServersMessage logs = (ListOfGameServersMessage)message;

		} else if (message instanceof AvatarStatusMessage) {
			AvatarStatusMessage asm = (AvatarStatusMessage)message;
			if (this.currentAvatar != null && asm.entityID == this.currentAvatar.getID()) {
				this.currentAvatar.setHealth(asm.health);
				//this.score = asm.score;
				this.currentAvatar.moveSpeed = asm.moveSpeed;
				this.currentAvatar.setJumpForce(asm.jumpForce);
				if (asm.damaged) {
					hud.showDamageBox();
				}
			}

		} else if (message instanceof GameOverMessage) {
			GameOverMessage gom = (GameOverMessage)message;
			if (gom.winningSide == -1) {
				//Globals.p("The game is a draw!");
				hud.showMessage("The game is a draw!");
				this.gameIsDrawn();
			} else if (gom.winningSide == this.side) {
				//Globals.p("You have won!");
				hud.showMessage("You have won!");
				this.playerHasWon();
			} else {
				//Globals.p("You have lost!");
				hud.showMessage("You have won!");
				this.playerHasLost();
			}

		} else if (message instanceof PlaySoundMessage) {
			PlaySoundMessage psm = (PlaySoundMessage)message;
			playSound(psm);

		} else if (message instanceof ModelBoundsMessage) {
			ModelBoundsMessage psm = (ModelBoundsMessage)message;
			addDebugBox(psm);

		} else if (message instanceof JoinGameFailedMessage) {
			JoinGameFailedMessage jgfm = (JoinGameFailedMessage)message;
			Globals.p("Join game failed: " + jgfm.reason);
			this.quit(jgfm.reason);

		} else if (message instanceof SetAvatarMessage) {
			SetAvatarMessage sam = (SetAvatarMessage)message;
			this.currentAvatarID = sam.avatarEntityID;
			this.setAvatar(this.entities.get(currentAvatarID));

			/*} else if (message instanceof NewClientOnlyEntity) {
			NewClientOnlyEntity ncoe = (NewClientOnlyEntity)message;
			createEntity(ncoe.data, ncoe.timestamp);
			 */

		} else if (message instanceof GameLogMessage) {
			GameLogMessage glm = (GameLogMessage)message;
			this.hud.appendToLog(glm.logEntry);

		} else if (message instanceof GunReloadingMessage) {
			GunReloadingMessage grm = (GunReloadingMessage)message;
			//this.reloading(true);
			//this.gunAngle = 0;
			this.finishedReloadAt = grm.duration_secs;
			this.currentlyReloading = true;

		} else {
			throw new RuntimeException("Unknown message type: " + message);
		}
	}


	/**
	 * Override if required
	 */
	protected void allEntitiesSent() {
	}


	private void setAvatar(IEntity e) {
		if (e != null) {
			if (e instanceof AbstractClientAvatar) {
				this.currentAvatar = (AbstractClientAvatar)e;//this.entities.get(currentAvatarID);
				if (Globals.DEBUG_AVATAR_SET) {
					Globals.p("Avatar for player is now " + currentAvatar);
				}

				Vector3f look = new Vector3f(15f, 1f, 15f);
				getCamera().lookAt(look, Vector3f.UNIT_Y); // Look somewhere
			} else {
				throw new RuntimeException("Player's avatar must be a subclass of " + AbstractClientAvatar.class.getSimpleName() + ".  This is a " + e);
			}
		} else {
			//Globals.pe("Trying to set null avatar");
		}
	}


	private void addDebugBox(ModelBoundsMessage msg) {
		if (msg.bounds instanceof BoundingBox) {
			BoundingBox bb = (BoundingBox)msg.bounds;
			Mesh box = new Box(bb.getXExtent(), bb.getYExtent(), bb.getZExtent());
			box.scaleTextureCoordinates(new Vector2f(bb.getXExtent(), bb.getYExtent()));
			Geometry debuggingBox = new Geometry("DebuggingBox", box);

			TextureKey key3 = new TextureKey( "Textures/fence.png");
			Texture tex3 = getAssetManager().loadTexture(key3);
			Material floor_mat = null;
			floor_mat = new Material(getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");
			floor_mat.setTexture("DiffuseMap", tex3);
			debuggingBox.setMaterial(floor_mat);
			debuggingBox.setLocalTranslation(msg.bounds.getCenter().x, msg.bounds.getCenter().y, msg.bounds.getCenter().z);
			debugNode.attachChild(debuggingBox);

			floor_mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
			debuggingBox.setQueueBucket(Bucket.Transparent);

			Globals.p("Created bounding box");
		}
	}


	protected abstract void playerHasWon();

	protected abstract void playerHasLost();

	protected abstract void gameIsDrawn();

	private void playSound(PlaySoundMessage psm) {
		this.playSound(psm.sound, psm.pos, psm.volume, psm.stream);
	}


	private void sendInputs() {
		if (this.currentAvatar != null) {
			// Send inputs
			if (networkClient.isConnected()) {
				//if (sendInputsInterval.hitInterval()) {  Don't need this since it's once a loop anyway
				this.networkClient.sendMessageToServer(new PlayerInputMessage(this.input));
				//}
			}
		}
	}


	protected final void createEntity(NewEntityData data, long timeToAdd) {
		IEntity e = actuallyCreateEntity(this, data);
		if (e != null) {
			if (e instanceof PhysicalEntity) {
				PhysicalEntity pe = (PhysicalEntity)e;
				pe.timeToAdd = timeToAdd;
			}
			this.addEntity(e);
		} else {
			Globals.p("Not creating entity type " + data.type);
			// It's not for this game, so ignore it
			//throw new RuntimeException("Cannot create entity type " + data.type);
		}

	}


	protected abstract IEntity actuallyCreateEntity(AbstractGameClient client, NewEntityData msg);


	@Override
	public void messageReceived(MyAbstractMessage message) {
		if (Globals.DEBUG_MSGS) {
			Globals.p("Rcvd " + message.getClass().getSimpleName());
		}

		synchronized (unprocessedMessages) {
			unprocessedMessages.add(message);
		}

	}


	protected abstract void gameStatusChanged(int oldStatus, int newStatus);


	@Override
	public void addEntity(IEntity e) {
		/*if (e.getID() <= 0) {
			throw new RuntimeException("ID must be positive if not client-side!");
		}*/

		//synchronized (entities) {
		if (e.getID() == 0) {
			throw new RuntimeException("No entity id!");
		}
		if (e.getID() > 0 && e.getGameID() != this.getGameID()) {
			throw new RuntimeException("Entity " + e + " is for game " + e.getGameID() + ", current game is " + this.getGameID());
		}
		if (e.getID() > 0) {
			if (this.entities.containsKey(e.getID())) {
				/*if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
					Globals.p("Remove entity " + e + " since it already exists");
				}
				// Replace it, since it might be an existing entity but its position has changed
				IEntity e2 = this.entities.get(e.getID());
				e2.remove();
				this.actuallyRemoveEntity(e2.getID());*/
				throw new RuntimeException("Entity " + e + " already exists"); // Don't replace it, as entity has linked to other stuff, like Abilities
			}
			this.entities.put(e.getID(), e);
		} else {
			if (this.getClientOnlyEntityById(e.getID()) != null) {
				throw new RuntimeException("Entity " + e + " already exists (client-only)"); // Don't replace it, as entity has linked to other stuff, like Abilities
			}
			this.clientOnlyEntities.add(e);
		}
		if (e.requiresProcessing()) {
			this.entitiesForProcessing.add(e);
		}
		if (e instanceof PhysicalEntity) {
			entitiesToAddToGame.add((PhysicalEntity)e);
		}
		/*			if (e instanceof IDrawOnHUD) {
				IDrawOnHUD doh = (IDrawOnHUD)e;
				this.hud.addItem(doh.getHUDItem());
			}
		 */
		if (e.getID() == currentAvatarID && e != this.currentAvatar) {
			// Avatar has been replaced
			this.setAvatar(e);
		}
		//}
		if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
			if (e instanceof PhysicalEntity) {
				PhysicalEntity pe = (PhysicalEntity)e;
				Globals.p("Created " + pe + " at " + pe.getWorldTranslation());
			} else {
				Globals.p("Created " + e); //((PhysicalEntity)e).getMainNode().getChild(0).getLocalRotation();
			}
		}
	}


	private void addEntityToGame(IEntity e) {
		boolean add = true;
		if (e instanceof ILaunchable) { // Don't add bullets until they are fired!
			ILaunchable il = (ILaunchable)e;
			add = il.hasBeenLaunched();
		}
		if (add) {
			PhysicalEntity pe = (PhysicalEntity)e;
			if (Globals.TOONISH) {
				//if (pe instanceof AbstractAISoldier) {
				makeToonish(pe.getMainNode());
				//}
			} //pe.getMainNode().getChild(0).getLocalRotation();
			// pe.getWorldTranslation();
			BoundingBox bb = (BoundingBox)pe.getMainNode().getWorldBound();
			boolean tooBig = bb.getXExtent() > nodeSize || bb.getYExtent() > nodeSize || bb.getZExtent() > nodeSize;
			if (!pe.moves && this.nodeSize > 0 && !tooBig) {
				int x = (int)bb.getCenter().x / this.nodeSize;
				int y = (int)bb.getCenter().y / this.nodeSize;
				int z = (int)bb.getCenter().z / this.nodeSize;

				String id = x + "_" + y + "_" + z;
				if (!this.nodes.containsKey(id)) {
					Node node = new Node(id);
					this.nodes.put(id, node);
					this.getGameNode().attachChild(node);
				}
				Node n = this.nodes.get(id);
				n.attachChild(pe.getMainNode());			
			} else {
				this.getGameNode().attachChild(pe.getMainNode()); // pe.getMainNode().getChild(0).getLocalRotation();
			}

			if (pe.simpleRigidBody != null) {
				this.getPhysicsController().addSimpleRigidBody(pe.simpleRigidBody);
				if (Globals.STRICT) {
					if (this.physicsController.getNumEntities() > this.entities.size()) {
						Globals.pe("Warning: more simple rigid bodies than entities!");
					}
				}
			}
			if (e instanceof IDrawOnHUD) {
				IDrawOnHUD doh = (IDrawOnHUD)e;
				if (doh.getHUDItem() != null) {
					this.hud.addItem(doh.getHUDItem());
				}
			}
		}
	}


	private void makeToonish(Spatial spatial) {
		try {
			if (spatial instanceof Node){
				Node n = (Node) spatial;
				for (Spatial child : n.getChildren()) {
					makeToonish(child);
				}
			} else if (spatial instanceof Geometry){
				Geometry g = (Geometry) spatial;
				Material m = g.getMaterial();
				//if (m.getMaterialDef().getMaterialParam("UseMaterialColors") != null) {
				Texture t = assetManager.loadTexture("Textures/toon.png");
				m.setTexture("ColorRamp", t);
				m.setBoolean("UseMaterialColors", true);
				m.setColor("Specular", ColorRGBA.Black);
				m.setColor("Diffuse", ColorRGBA.White);
				m.setBoolean("VertexLighting", true);
				//}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	private void removeAllEntities() {
		Globals.p("REMOVING ALL ENTITIES...");
		this.entitiesToAddToGame.clear();
		while (this.entities.size() > 0) {
			Iterator<IEntity> it = this.entities.values().iterator();
			IEntity e = it.next();
			e.remove();
			it.remove(); //this.entitiesToRemove
		}
		this.entitiesForProcessing.clear();
		this.clientOnlyEntities.clear();
		this.nodes.clear();
		this.gameNode.detachAllChildren();
		this.gameNode.removeFromParent();

		if (Globals.STRICT) {
			if (this.physicsController.getNumEntities() > this.entities.size()) {
				Globals.pe("Warning: more simple rigid bodies than entities!");
			}
		}
	}


	@Override
	public void removeEntity(int id) {
		if (id > 0) {
			if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
				IEntity e = this.entities.get(id);
				if (e != null) {
					Globals.p("Going to remove entity " + id + ":" + e);
				} else {
					Globals.p("Going to remove entity " + id);
				}
			}
			this.entitiesToRemove.add(id);
			//actuallyRemoveEntity(id);
		} else {
			/*if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
				IEntity e = this.clientOnlyEntities.get(id);
				if (e != null) {
					Globals.p("Going to remove CO entity " + id + ":" + e);
				} else {
					Globals.p("Going to remove CO entity " + id);
				}
			}*/
			//this.clientOnlyEntitiesToRemove.add(id);
			this.actuallyRemoveClientOnlyEntity(id);
		}
	}


	/*
	 * Note that an entity is responsible for clearing up it's own data!  This method should only remove the server's knowledge of the entity.  e.remove() does all the hard work.
	 */
	private void actuallyRemoveEntity(int id) {
		this.entitiesToRemove.removeFirstOccurrence(id);
		synchronized (entities) {
			IEntity e = this.entities.get(id);
			if (e != null) {
				if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
					Globals.p("Actually removing entity " + id + ":" + e);
				}
				this.entities.remove(id);
				if (e.requiresProcessing()) {
					this.entitiesForProcessing.remove(e);
				}
				if (e == this.currentAvatar) {
					if (Globals.DEBUG_AVATAR_SET) {
						Globals.p("Avatar for player removed");
					}
					this.currentAvatar = null;
				}
			} else {
				//Globals.pe("Entity id " + id + " not found for removal");
			}
		}
		if (Globals.STRICT) {
			if (this.entities.containsKey(id)) {
				Globals.pe("Entity still exists!");
			}
		}
	}


	@Override
	public void onAction(String name, boolean value, float tpf) {
		if (name.equalsIgnoreCase(QUIT)) {
			if (value) {
				quit("User chose to.");
			}
		} else if (name.equalsIgnoreCase(RELOAD)) {

		} else if (name.equalsIgnoreCase(TEST)) {
			if (value) {
				//new AbstractHUDImage(this, this.getNextEntityID(), this.hud, "Textures/text/winner.png", this.cam.getWidth(), this.cam.getHeight(), 5);
				//this.avatar.setWorldTranslation(new Vector3f(10, 10, 10));
			}
		}
	}


	public void quit(String reason) {
		Globals.p("quitting: " + reason);
		if (networkClient != null && this.networkClient.isConnected()) {
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
		//System.exit(0); No, since we might be running multiple!
	}


	@Override
	public boolean isServer() {
		return false;
	}


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


	@Override
	public void collisionOccurred(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		PhysicalEntity pea = a.userObject;
		PhysicalEntity peb = b.userObject;

		if (pea instanceof INotifiedOfCollision) {
			INotifiedOfCollision ic = (INotifiedOfCollision)pea;
			ic.collided(peb);
		}
		if (peb instanceof INotifiedOfCollision) {
			INotifiedOfCollision ic = (INotifiedOfCollision)peb;
			ic.collided(pea);
		}
	}


	@Override
	public int getNextEntityID() {
		return nextEntityID.decrementAndGet(); // Client-only entities are negative
	}


	@Override
	public Node getGameNode() {
		return gameNode;
	}


	private void showPlayersWeapon() {
		if (playersWeaponNode == null) {
			playersWeaponNode = new Node("PlayersWeapon");
			this.getGameNode().attachChild(playersWeaponNode);
		}
		playersWeaponNode.detachAllChildren();
		weaponModel = getPlayersWeaponModel();
		if (weaponModel != null) {
			playersWeaponNode.attachChild(weaponModel);
		}
	}


	private void reloading(float tpf_secs, boolean started) {
		float gunRotSpeed = 400;
		float diff = (gunRotSpeed * tpf_secs);

		this.finishedReloadAt -= tpf_secs;
		//Globals.p("reloading(" + started + ")"); // weaponModel.getLocalRotation();
		if (started) {
			if (gunAngle < 90) {
				gunAngle += diff;
				weaponModel.rotate((float)Math.toRadians(-diff), 0f, 0f);
			}
		} else {
			if (gunAngle > 0) {
				gunAngle -= diff;
				weaponModel.rotate((float)Math.toRadians(diff), 0f, 0f);
			} else {
				currentlyReloading = false;
			}
		}
		if (Globals.DEBUG_GUN_ROTATION) {
			Globals.p("Gun angle = " + gunAngle);
		}
	}


	protected abstract Spatial getPlayersWeaponModel();


	@Override
	public long getRenderTime() {
		return this.renderTime;
	}


	@Override
	public IEntity getEntity(int id) {
		return this.entities.get(id);
	}


	@Override
	public int getGameID() {
		return this.gameData.gameID;
	}


	@Override
	public synchronized int getNumEntities() {
		return this.entities.size();
	}


	@Override
	public void processConsoleInput(String s) {
		//Globals.p("Received input: " + s);
		if (s.equalsIgnoreCase("help") || s.equalsIgnoreCase("?")) {
			Globals.p("stats, entities");
		} else if (s.equalsIgnoreCase("stats")) {
			showStats();
		} else if (s.equalsIgnoreCase("entities") || s.equalsIgnoreCase("e")) {
			listEntities();
		} else {
			Globals.p("Unknown command: " + s);
		}

	}


	private void showStats() {
		Globals.p("Num Entities: " + this.entities.size());
		Globals.p("Num Entities for proc: " + this.entitiesForProcessing.size());
	}


	private void listEntities() {
		synchronized (entities) {
			// DO server-side first
			int count = 0;
			for (IEntity e : entities.values()) {
				if (e.getID() > 0) {
					Globals.p("Entity " + e.getID() + ": " + e.getName() + " (" + e + ")");
					count++;
				}
			}
			Globals.p("Total server entities:" + count);

			// DO client-side
			count = 0;
			for (IEntity e : entities.values()) {
				if (e.getID() < 0) {
					Globals.p("CO Entity " + e.getID() + ": " + e.getName() + " (" + e + ")");
					count++;
				}
			}
			Globals.p("Total CO entities:" + count);
		}
	}


	@Override
	public void sendMessage(MyAbstractMessage msg) {
		this.networkClient.sendMessageToServer(msg);

	}


	/*
	 * Note that an entity is responsible for clearing up it's own data!  This method should only remove the server's knowledge of the entity.  e.remove() does all the hard work.
	 */
	private void actuallyRemoveClientOnlyEntity(int id) {
		IEntity e = getClientOnlyEntityById(id);//this.clientOnlyEntities.get(id);
		if (e != null) {
			if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
				Globals.p("Actually removing CO entity " + id + ":" + e);
			}
			this.clientOnlyEntities.remove(e);
			if (e.requiresProcessing()) {
				this.entitiesForProcessing.remove(e);
			}
		} else {
			Globals.pe("Entity id " + id + " not found for removal");
			//this.entitiesForProcessing.remove(id); // Just in case, otherwise we'll try and remove it foreer
		}
	}


	private IEntity getClientOnlyEntityById(int id) {
		for (IEntity e : this.clientOnlyEntities) {
			if (e.getID() == id) {
				return e;
			}
		}
		return null;
	}


	@Override
	public int getPlayerID() {
		return playerID;
	}


	@Override
	public boolean canCollide(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		PhysicalEntity pa = a.userObject; //pa.getMainNode().getWorldBound();
		PhysicalEntity pb = b.userObject; //pb.getMainNode().getWorldBound();

		return canCollide(pa, pb);
	}


	protected void setupFilters() {
		renderManager.setAlphaToCoverage(true);
		if (renderer.getCaps().contains(Caps.GLSL100)){
			fpp = new FilterPostProcessor(assetManager);
			//fpp.setNumSamples(4);
			int numSamples = getContext().getSettings().getSamples();
			if( numSamples > 0 ) {
				fpp.setNumSamples(numSamples); 
			}

			/*CartoonEdgeFilter toon=new CartoonEdgeFilter();
			toon.setEdgeColor(ColorRGBA.Yellow);
			toon.setEdgeWidth(0.5f);
			toon.setEdgeIntensity(1.0f);
			toon.setNormalThreshold(0.8f);
			fpp.addFilter(toon);*/
			viewPort.addProcessor(fpp);
		}
	}


	@Override
	public SimpleGameData getGameData() {
		return this.gameData;
	}


	@Override
	public void playSound(String _sound, Vector3f _pos, float _volume, boolean _stream) {
		try {
			AudioNode node = new AudioNode(this.getAssetManager(), _sound, _stream ? AudioData.DataType.Stream : AudioData.DataType.Buffer);
			node.setLocalTranslation(_pos);
			node.setVolume(_volume);
			node.setLooping(false);
			node.play();

			this.gameNode.attachChild(node);

			// Create thread to remove it
			this.enqueue(new Callable<Spatial>() {
				public Spatial call() throws Exception {
					try {
						Thread.sleep((long)node.getPlaybackTime() + 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					node.removeFromParent();
					return node;
				}
			});

		} catch (AssetNotFoundException ex) {
			//ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
