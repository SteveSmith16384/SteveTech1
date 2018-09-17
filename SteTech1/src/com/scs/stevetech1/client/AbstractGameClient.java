package com.scs.stevetech1.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.BackingStoreException;

import com.ding.effect.outline.filter.OutlinePreFilter;
import com.ding.effect.outline.filter.OutlineProFilter;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bounding.BoundingBox;
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
import com.jme3.renderer.ViewPort;
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
import com.scs.stevetech1.client.povweapon.IPOVWeapon;
import com.scs.stevetech1.components.IAddedImmediately;
import com.scs.stevetech1.components.IAnimatedClientSide;
import com.scs.stevetech1.components.IDrawOnHUD;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IKillable;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.components.IPlayerControlled;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.data.SimplePlayerData;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractBullet;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.entities.Entity;
import com.scs.stevetech1.entities.ExplosionShard;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.input.MouseAndKeyboardCamera;
import com.scs.stevetech1.netmessages.AbilityUpdateMessage;
import com.scs.stevetech1.netmessages.AvatarStartedMessage;
import com.scs.stevetech1.netmessages.AvatarStatusMessage;
import com.scs.stevetech1.netmessages.EntityKilledMessage;
import com.scs.stevetech1.netmessages.EntityUpdateData;
import com.scs.stevetech1.netmessages.EntityUpdateMessage;
import com.scs.stevetech1.netmessages.GameLogMessage;
import com.scs.stevetech1.netmessages.GameOverMessage;
import com.scs.stevetech1.netmessages.GeneralCommandMessage;
import com.scs.stevetech1.netmessages.ModelBoundsMessage;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.netmessages.NewEntityData;
import com.scs.stevetech1.netmessages.NewEntityMessage;
import com.scs.stevetech1.netmessages.NumEntitiesMessage;
import com.scs.stevetech1.netmessages.PingMessage;
import com.scs.stevetech1.netmessages.PlaySoundMessage;
import com.scs.stevetech1.netmessages.PlayerInputMessage;
import com.scs.stevetech1.netmessages.PlayerLeftMessage;
import com.scs.stevetech1.netmessages.RemoveEntityMessage;
import com.scs.stevetech1.netmessages.SetAvatarMessage;
import com.scs.stevetech1.netmessages.ShowMessageMessage;
import com.scs.stevetech1.netmessages.SimpleGameDataMessage;
import com.scs.stevetech1.netmessages.connecting.GameSuccessfullyJoinedMessage;
import com.scs.stevetech1.netmessages.connecting.HelloMessage;
import com.scs.stevetech1.netmessages.connecting.JoinGameFailedMessage;
import com.scs.stevetech1.netmessages.connecting.JoinGameRequestMessage;
import com.scs.stevetech1.netmessages.lobby.ListOfGameServersMessage;
import com.scs.stevetech1.networking.IGameMessageClient;
import com.scs.stevetech1.networking.IMessageClientListener;
import com.scs.stevetech1.networking.KryonetGameClient;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.systems.EntityRemovalSystem;
import com.scs.stevetech1.systems.client.AnimationSystem;
import com.scs.stevetech1.systems.client.SoundSystem;

import ssmith.lang.NumberFunctions;
import ssmith.util.AverageNumberCalculator;
import ssmith.util.ConsoleInputListener;
import ssmith.util.FixedLoopTime;
import ssmith.util.RealtimeInterval;
import ssmith.util.TextConsole;

public abstract class AbstractGameClient extends SimpleApplication implements IClientApp, IEntityController, 
ActionListener, IMessageClientListener, ICollisionListener<PhysicalEntity>, ConsoleInputListener { 

	private static final String JME_SETTINGS_NAME = "jme_client_settings.txt";

	// Global controls
	private static final String QUIT = "Quit";
	protected static final String TEST = "Test";

	private static AtomicInteger nextEntityID = new AtomicInteger(-1);

	public HashMap<Integer, IEntity> entities = new HashMap<>(1000); // All ents are added to this immediately, but not added to root node until the right time
	public ArrayList<IEntity> entitiesForProcessing = new ArrayList<>(100); // Entities that we need to iterate over in game loop
	protected LinkedList<PhysicalEntity> entitiesToAddToRootNode = new LinkedList<PhysicalEntity>(); // Entities to add to RootNode, as we don't add them immed

	protected SimplePhysicsController<PhysicalEntity> physicsController; // Checks all collisions
	protected FixedLoopTime loopTimer; // Keep client and server running at the same time

	public int tickrateMillis, clientRenderDelayMillis, timeoutMillis; // todo - combine into class?
	private RealtimeInterval sendPingInterval = new RealtimeInterval(Globals.PING_INTERVAL_MS);
	private RealtimeInterval sendInputsInterval;// = new RealtimeInterval(Globals.PING_INTERVAL_MS);
	private ValidateClientSettings validClientSettings;
	public IGameMessageClient networkClient;
	public boolean rcvdHello = false;
	public IInputDevice input;
	public IPOVWeapon povWeapon;

	public AbstractClientAvatar currentAvatar;
	public int currentAvatarID = -1; // In case the avatar physical entity gets replaced, we can re-assign it
	public int playerID = -1;
	public byte side = -1;
	private AverageNumberCalculator pingCalc = new AverageNumberCalculator(4);
	public long pingRTT;
	private long clientToServerDiffTime; // Add to current time to get server time
	protected boolean joinedGame = false;
	public SimpleGameData gameData;
	public ArrayList<SimplePlayerData> playersList;
	private SoundSystem soundSystem;
	private int expectedNumEntities = -1; // So we can show "% complete"

	protected Node gameNode = new Node("GameNode");
	protected Node debugNode = new Node("DebugNode");
	protected FilterPostProcessor fpp;

	private List<MyAbstractMessage> unprocessedMessages = new LinkedList<>();

	public long serverTime, renderTime;
	private float mouseSens;
	private String consoleInput;
	private boolean showHistory = false; // SHowing history cam (feature in progress)

	protected Thread connectingThread;
	public Exception lastConnectException;

	// Subnodes
	private int nodeSize = Globals.SUBNODE_SIZE;
	public HashMap<String, Node> nodes;

	// Entity systems
	private AnimationSystem animSystem;
	private EntityRemovalSystem entityRemovalSystem;


	protected AbstractGameClient(ValidateClientSettings _validClientSettings, String appTitle, String logoImage,   
			int _tickrateMillis, int _clientRenderDelayMillis, int _timeoutMillis, float _mouseSens) { 
		super();

		validClientSettings = _validClientSettings;

		tickrateMillis = _tickrateMillis;
		clientRenderDelayMillis = _clientRenderDelayMillis;
		timeoutMillis = _timeoutMillis;

		Globals.showWarnings();

		mouseSens = _mouseSens;

		settings = new AppSettings(true);
		try {
			settings.load(JME_SETTINGS_NAME);
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		settings.setUseJoysticks(true);

		settings.setTitle(!Globals.RELEASE_MODE ? "Client" : appTitle);// + " (v" + Settings.VERSION + ")");
		settings.setSettingsDialogImage(logoImage);

		setSettings(settings);
		setPauseOnLostFocus(false); // Needs to always be in sync with server!

		try {
			settings.save(JME_SETTINGS_NAME);
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}

		// Remove the automatically-added FlyCam
		FlyCamAppState state = stateManager.getState(FlyCamAppState.class);
		if (state != null) {
			this.stateManager.detach(state);
		}

		this.loopTimer = new FixedLoopTime(tickrateMillis);
		this.physicsController = new SimplePhysicsController<PhysicalEntity>(this, Globals.SUBNODE_SIZE);
		this.animSystem = new AnimationSystem(this);
		this.entityRemovalSystem = new EntityRemovalSystem(this);
		this.sendInputsInterval = new RealtimeInterval(tickrateMillis);
		nodes = new HashMap<String, Node>();

	}


	@Override
	public void simpleInitApp() {
		assetManager.registerLocator("assets/", FileLocator.class); // default
		assetManager.registerLocator("assets/", ClasspathLocator.class);

		// Clear existing mappings
		getInputManager().clearMappings();
		getInputManager().clearRawInputListeners();

		input = new MouseAndKeyboardCamera(getCamera(), getInputManager(), mouseSens);
		addDefaultKeyboardMappings();

		cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.001f, Globals.CAM_VIEW_DIST);

		soundSystem = new SoundSystem(this.getAssetManager(), this.getGameNode());

		setUpLight();

		if (!Globals.RELEASE_MODE) {
			this.getRootNode().attachChild(this.debugNode);
		}

		if (Globals.RECORD_VID) {
			Globals.p("Recording video!");
			VideoRecorderAppState video_recorder = new VideoRecorderAppState();
			stateManager.attach(video_recorder);
		}

		if (!Globals.SHOW_FPS_STATS) {
			// Turn off stats
			setDisplayFps(false);
			setDisplayStatView(false);
		}

		fpp = new FilterPostProcessor(assetManager);
		viewPort.addProcessor(fpp);

		new TextConsole(this);

		loopTimer.start();
	}


	private void addDefaultKeyboardMappings() {
		getInputManager().addMapping(QUIT, new KeyTrigger(KeyInput.KEY_ESCAPE));
		getInputManager().addListener(this, QUIT);            

		if (!Globals.RELEASE_MODE) {
			getInputManager().addMapping(TEST, new KeyTrigger(KeyInput.KEY_T));
			getInputManager().addListener(this, TEST);            
		}
	}


	public void connect(String gameServerIP, int gamePort, boolean thread) {
		lastConnectException = null;
		final AbstractGameClient c = this;

		connectingThread = new Thread("ConnectingToServer") {

			@Override
			public void run() {
				try {
					networkClient = new KryonetGameClient(gameServerIP, gamePort, gamePort, c, timeoutMillis, getListofMessageClasses());
					Globals.p("Connected");
				} catch (Exception e) {
					lastConnectException = e;
				}
			}
		};

		if (thread) {
			connectingThread.start();
		} else {
			connectingThread.run();
			connectingThread = null;
			if (this.lastConnectException != null) {
				throw new RuntimeException("Unable to connect", this.lastConnectException);
			}
		}

	}


	public boolean isConnecting() {
		return connectingThread != null && connectingThread.isAlive();
	}


	protected abstract Class[] getListofMessageClasses();


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
	public void simpleUpdate(float tpfSecs) {
		if (tpfSecs > 1) {
			tpfSecs = 1;
		}

		if (Globals.STRICT) {
			if (this.physicsController.getNumEntities() > this.entities.size()) { //this.rootNode
				Globals.pe("Warning: more simple rigid bodies than entities!");
			}
		}
		checkConsoleInput();

		try {
			serverTime = System.currentTimeMillis() + this.clientToServerDiffTime;
			if (!this.showHistory) {
				renderTime = serverTime - clientRenderDelayMillis; // Render from history
			} else {
				renderTime = serverTime - Globals.HISTORY_DURATION_MILLIS;
			}

			if (networkClient != null && networkClient.isConnected()) {

				processMessages();

				if (!Globals.DEBUG_MSGS) { // Don't send pings if we're debugging msgs
					if (this.isConnected() && sendPingInterval.hitInterval()) {
						networkClient.sendMessageToServer(new PingMessage(false, 0));
					}
				}

				addAndRemoveEntities();

				if (joinedGame) {

					if (Globals.FORCE_CLIENT_SLOWDOWN || sendInputsInterval.hitInterval()) {
						this.sendInputs();
					}

					if (Globals.SHOW_LATEST_AVATAR_POS_DATA_TIMESTAMP) {
						try {
							long timeDiff = this.currentAvatar.historicalPositionData.getMostRecent().serverTimestamp - renderTime;
							//this.hud.setDebugText("Latest Data is " + timeDiff + " newer than we need");
						} catch (Exception ex) {
							// do nothing, no data yet
						}
					}

					if (Globals.STRICT) {
						// Check all entities that require processing are in the correct list
						for(IEntity e : this.entities.values()) {
							if (e.requiresProcessing()) {
								if (!this.entitiesForProcessing.contains(e)) {
									throw new RuntimeException("Processed entity " + e + " not in process list!");
								}
							}
						}
						// Check all nodes have an entity in the list
						// todo
					}

					iterateThroughEntities(tpfSecs);

					if (this.povWeapon != null) {
						this.povWeapon.update(tpfSecs);
					}

				}
			}

			soundSystem.process();

			if (Globals.STRICT) {
				checkNodesExistInEntities();
			}

			if (Globals.FORCE_CLIENT_SLOWDOWN) {
				loopTimer.waitForFinish(); // Keep clients and server running at same speed
				loopTimer.start();
			}

		} catch (Exception ex) {
			Globals.HandleError(ex);
			this.quit("Error: " + ex);
		}
	}


	private void addAndRemoveEntities() {
		this.entityRemovalSystem.actuallyRemoveEntities();

		// Add entities
		if (entitiesToAddToRootNode.size() > 0) {
			for (int i=0 ; i<entitiesToAddToRootNode.size() ; i++) {
				PhysicalEntity pe = entitiesToAddToRootNode.get(i);
				if (pe.timeToAdd < renderTime) {
					if (pe.getID() < 0 || this.entities.containsKey(pe.getID())) { // Check it is still in the game
						this.addEntityToGame(pe);
					} else {
						Globals.p("Not adding " + pe + " to the game as it's not in it any more");
						if (pe.getMainNode().getParent() != null) {
							pe.getMainNode().removeFromParent();
						}
					}
					entitiesToAddToRootNode.remove(i);
					i--;
				}
			}
		}
	}


	private void processMessages() {
		synchronized (unprocessedMessages) {
			Iterator<MyAbstractMessage> mit = this.unprocessedMessages.iterator();
			while (mit.hasNext()) {
				MyAbstractMessage message = mit.next();
				if (message.scheduled) {
					if (message.timestamp > renderTime) {
						continue;
					}
				}
				boolean accepted = this.handleMessage(message);
				if (accepted) {
					mit.remove();
				}
			}
		}

	}


	private void iterateThroughEntities(float tpfSecs) {
		// Loop through each entity and process them
		for (int i=0 ; i<this.entitiesForProcessing.size() ; i++) {
			IEntity e = this.entitiesForProcessing.get(i);
			if (!e.isMarkedForRemoval()) {
				if (e instanceof IPlayerControlled) {
					IPlayerControlled p = (IPlayerControlled)e;
					p.resetPlayerInput();
				}
				if (e instanceof PhysicalEntity) {
					PhysicalEntity pe = (PhysicalEntity)e;

					pe.calcPosition(renderTime, tpfSecs); // Must be before we process physics as this calcs additionalForce
					pe.processChronoData(renderTime, tpfSecs);

					if (Globals.STRICT) {
						// Check the client side object is kinemtic - almost all should be since the server controls them
						if (e instanceof AbstractClientAvatar == false && e instanceof ExplosionShard == false && e instanceof AbstractBullet == false) {
							if (pe.simpleRigidBody != null) {
								if (pe.simpleRigidBody.movedByForces()) {
									Globals.pe("Warning: client-side entity " + pe + " not kinematic!");
								}
							}
						}
					}

				}

				if (e instanceof IProcessByClient) {
					IProcessByClient pbc = (IProcessByClient)e;
					pbc.processByClient(this, tpfSecs); // Mainly to process client-side movement of the avatar
				}

				if (e instanceof IAnimatedClientSide) {
					IAnimatedClientSide pbc = (IAnimatedClientSide)e;
					this.animSystem.process(pbc, tpfSecs);
				}

				if (e instanceof IDrawOnHUD) {
					IDrawOnHUD doh = (IDrawOnHUD)e;
					doh.drawOnHud(this.getHudNode(), cam);
				}
			}
		}

	}


	/**
	 * Override if required
	 */
	public Node getHudNode() {
		return this.getGuiNode();
	}


	private void checkNodesExistInEntities() {
		if (Globals.STRICT) {
			// Check all Nodes are in the entity list
			for (Spatial n : this.gameNode.getChildren()) {
				IEntity e = n.getUserData(Globals.ENTITY);
				if (e != null) {
					if (e.getID() > 0) {
						if (e instanceof PhysicalEntity) {
							//PhysicalEntity pe = (PhysicalEntity)e;
							if (!this.entities.containsValue(e)) {
								Globals.pe("WARNING: Spatial " + e + " not found in entity list!");
							}
						}
					}
				}
			}
		}
	}


	protected boolean handleMessage(MyAbstractMessage message) {
		/*if (message instanceof PingMessage) {
			PingMessage pingMessage = (PingMessage) message;
			if (!pingMessage.s2c) {
				long rtt = System.currentTimeMillis() - pingMessage.originalSentTime;
				this.pingRTT = this.pingCalc.add(rtt);
				clientToServerDiffTime = pingMessage.responseSentTime - pingMessage.originalSentTime - (pingRTT/2); // If running on the same server, this should be 0! (or close enough)

			} else {
				pingMessage.responseSentTime = System.currentTimeMillis();
				networkClient.sendMessageToServer(message); // Send it straight back
			}

		} else */if (message instanceof ShowMessageMessage) {
			ShowMessageMessage gsm = (ShowMessageMessage)message;
			this.showMessage(gsm.msg);

		} else if (message instanceof GameSuccessfullyJoinedMessage) {
			GameSuccessfullyJoinedMessage npcm = (GameSuccessfullyJoinedMessage)message;
			if (this.playerID <= 0) {
				this.playerID = npcm.playerID;
				this.side = npcm.side;
				joinedGame = true;
			} else {
				throw new RuntimeException("Already received GameSuccessfullyJoinedMessage");
			}

		} else if (message instanceof HelloMessage) {
			this.rcvdHello = true;
			receivedWelcomeMessage();

		} else if (message instanceof SimpleGameDataMessage) {
			SimpleGameData oldGameData = this.gameData;
			SimpleGameDataMessage gsm = (SimpleGameDataMessage)message;
			this.gameData = gsm.gameData;
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
							//Globals.p("Ignoring new entity " + e + " as we already know about it");
						}
					}
				} else {
					//Globals.p("Ignoring NewEntityMessage for game " + newEntityMessage.gameId);
					// It's not for this game, so ignore it
				}
			} else {
				//Globals.p("Ignoring NewEntityMessage as we have no game data");
				//throw new RuntimeException("No game data!");
				return false; // Wait for game data
			}

		} else if (message instanceof EntityUpdateMessage) {
			EntityUpdateMessage mainmsg = (EntityUpdateMessage)message;
			for(EntityUpdateData eud : mainmsg.data) {
				IEntity e = this.entities.get(eud.entityID);
				if (e != null) {
					if (Globals.DEBUG_NO_UPDATE_MSGS) {
						Globals.p("Received EntityUpdateMessage for " + e);
					}
					if (Globals.DEBUG_CPU_HUD_TEXT) {
						if (e.getName().equalsIgnoreCase("computer")) {
							Globals.p("Sending computer update");
						}
					}								
					PhysicalEntity pe = (PhysicalEntity)e;
					pe.addPositionData(eud, mainmsg.timestamp);
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

		} else if (message instanceof RemoveEntityMessage) {
			RemoveEntityMessage rem = (RemoveEntityMessage)message;
			if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
				Globals.p("Rcvd msg to remove entity " + rem.entityID);
			}
			Entity e = (Entity)this.entities.get(rem.entityID);
			if (e != null) {
				this.entityRemovalSystem.markEntityForRemoval(e);
			} else {
				if (Globals.STRICT) {
					Globals.p("Ignoring msg to remove entity " + rem.entityID + " as we have no record of it");
				}
			}
		} else if (message instanceof GeneralCommandMessage) {
			GeneralCommandMessage msg = (GeneralCommandMessage)message;
			//if (msg.gameID == this.getGameID()) { Might not have game id
			Globals.p("Rcvd GeneralCommandMessage: " + msg.command.toString());
			if (msg.command == GeneralCommandMessage.Command.AllEntitiesSent) { // We now have enough data to start
				this.expectedNumEntities = -1;
				this.appendToLog("All entities received");
				allEntitiesReceived();
			} else if (msg.command == GeneralCommandMessage.Command.RemoveAllEntities) { // We now have enough data to start
				this.removeAllEntities();
			} else if (msg.command == GeneralCommandMessage.Command.GameRestarting) {
				this.appendToLog("Game restarting...");
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
			if (killed != null) {
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
					this.currentAvatar.setAlive(false);
					this.avatarKilled();
					if (Globals.SHOW_VIEW_FROM_KILLER_ON_DEATH) {
						this.showHistory = true;
					}
				}
			}
			/*} else if (message instanceof EntityLaunchedMessage) {
			EntityLaunchedMessage elm = (EntityLaunchedMessage)message;
			if (elm.playerID != this.playerID) {
				IEntity shooter = this.entities.get(elm.shooterId);
				AbstractBullet bullet = (AbstractBullet)this.entities.get(elm.bulletEntityID);
				bullet.launch(shooter, elm.startPos, elm.dir);
			} else {
				// It was us that launched it in the first place, so we've already done the work!
				Globals.p("Ignoring entity launched message");
			}*/

		} else if (message instanceof AvatarStartedMessage) {
			if (Globals.DEBUG_PLAYER_RESTART) {
				Globals.p("Rcvd AvatarStartedMessage");
			}
			AvatarStartedMessage asm = (AvatarStartedMessage) message;
			if (this.currentAvatar != null && asm.entityID == this.currentAvatar.getID()) {
				currentAvatar.setAlive(true);
				this.avatarStarted();
				this.showHistory = false;

				// Point camera fwds again
				Vector3f look = cam.getLocation().add(Vector3f.UNIT_X);
				look.y = cam.getLocation().y;
				cam.lookAt(look, Vector3f.UNIT_Y);
				cam.update();
			}

		} else if (message instanceof ListOfGameServersMessage) {
			ListOfGameServersMessage logs = (ListOfGameServersMessage)message;

		} else if (message instanceof AvatarStatusMessage) {
			AvatarStatusMessage asm = (AvatarStatusMessage)message;
			if (this.currentAvatar != null && asm.entityID == this.currentAvatar.getID()) {
				this.currentAvatar.setHealth(asm.health);
				if (asm.damaged) {
					showDamageBox();
				}
			}

		} else if (message instanceof GameOverMessage) {
			GameOverMessage gom = (GameOverMessage)message;
			if (gom.winningSide == -1) {
				showMessage("The game is a draw!");
				this.gameIsDrawn();
			} else if (gom.winningSide == this.side) {
				showMessage("You have won!");
				this.playerHasWon();
			} else {
				showMessage("You have won!");
				this.playerHasLost();
			}

		} else if (message instanceof PlaySoundMessage) {
			PlaySoundMessage psm = (PlaySoundMessage)message;
			this.playSound(psm.soundId, psm.entityId, psm.pos, psm.volume, psm.stream);

		} else if (message instanceof ModelBoundsMessage) {
			ModelBoundsMessage psm = (ModelBoundsMessage)message;
			addDebugBox(psm);

		} else if (message instanceof JoinGameFailedMessage) {
			JoinGameFailedMessage jgfm = (JoinGameFailedMessage)message;
			Globals.p("Join game failed: " + jgfm.reason);
			this.showMessage("Join game failed: " + jgfm.reason);
			//this.quit(jgfm.reason);

		} else if (message instanceof SetAvatarMessage) {
			SetAvatarMessage sam = (SetAvatarMessage)message;
			this.currentAvatarID = sam.avatarEntityID;
			this.setAvatar(this.entities.get(currentAvatarID));

		} else if (message instanceof GameLogMessage) {
			GameLogMessage glm = (GameLogMessage)message;
			this.appendToLog(glm.logEntry);

			/*} else if (message instanceof GunReloadingMessage) {
			GunReloadingMessage grm = (GunReloadingMessage)message;
			//this.finishedReloadAt = grm.duration_secs;
			//this.currentlyReloading = true;
			if (this.povWeapon != null) {
				povWeapon.reload(grm.durationSecs);	
			}*/

		} else if (message instanceof NumEntitiesMessage) {
			NumEntitiesMessage nem = (NumEntitiesMessage)message;
			expectedNumEntities = nem.num;

		} else {
			throw new RuntimeException("Unknown message type: " + message);
		}

		return true;
	}


	/**
	 * Override if required
	 */
	protected void showDamageBox() {
	}


	/**
	 * Override if required
	 */
	protected void showMessage(String msg) {
		Globals.p(msg);
	}


	/**
	 * Override if required
	 */
	protected void appendToLog(String msg) {
		Globals.p(msg);
	}


	/**
	 * Override this if you don't want to join a game immediately after connecting.
	 */
	protected void receivedWelcomeMessage() {
		this.joinGame();
	}


	public void joinGame() {
		/*if (playerName == null || playerName.length() == 0) {
			playerName = "Player_" + NumberFunctions.rnd(1, 1000);
		}
		if (this.rcvdHello == false) {
			throw new RuntimeException("Trying to join game before receiving hello");
		}*/
		networkClient.sendMessageToServer(new JoinGameRequestMessage(validClientSettings.gameCode, validClientSettings.clientVersion, getPlayerName(), validClientSettings.key));
	}


	/**
	 * Override if required.
	 * @return
	 */
	protected String getPlayerName() {
		return "Player_" + NumberFunctions.rnd(1, 1000);
	}


	/**
	 * Override if required
	 */
	protected void allEntitiesReceived() {
		this.getRootNode().attachChild(this.gameNode); // todo - do once all actually added?
	}


	protected void setAvatar(IEntity e) {
		if (e != null) {
			if (e instanceof AbstractClientAvatar) {
				this.currentAvatar = (AbstractClientAvatar)e;
				if (Globals.DEBUG_AVATAR_SET) {
					Globals.p("Avatar for player is now " + currentAvatar);
				}
				Vector3f look = new Vector3f(-15f, getCamera().getLocation().y, -15f);
				getCamera().lookAt(look, Vector3f.UNIT_Y); // Look somewhere
			} else {
				throw new RuntimeException("Player's avatar must be a subclass of " + AbstractClientAvatar.class.getSimpleName() + ".  This is a " + e);
			}
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


	protected void playerHasWon() {
		// Override if required
	}


	protected void playerHasLost() {
		// Override if required
	}


	protected void gameIsDrawn() {
		// Override if required
	}


	private void sendInputs() {
		if (this.currentAvatar != null && input != null) {
			this.networkClient.sendMessageToServer(new PlayerInputMessage(this.input));
		}
	}


	protected final void createEntity(NewEntityData data, long timeToAdd) {
		IEntity e = actuallyCreateEntity(this, data);
		if (e != null) {
			if (e instanceof PhysicalEntity) {
				PhysicalEntity pe = (PhysicalEntity)e;
				boolean addImmed = false;
				if (pe instanceof IAddedImmediately) {
					IAddedImmediately ab = (IAddedImmediately)pe;
					addImmed = ab.shouldClientAddItImmediately(); // e.g. Bullet was fired by another player
				}
				if (addImmed) {
					pe.timeToAdd = -1;
				} else {
					pe.timeToAdd = timeToAdd;
				}
			}
			this.addEntity(e);
			//Globals.p("Finished creating entity " + data.type);
		} else {
			// It's maybe not for this game, or its for our own bullet, so ignore it
			//Globals.p("Not creating entity type " + data.type);
		}

		// Update % complete
		if (this.expectedNumEntities > 0) {
			float frac = (this.entities.size()) / (float)this.expectedNumEntities;
			int fracPC = (int)(frac * 100);
			//Globals.p("Entities: " + fracPC + "%");
			this.showMessage("Loading entities: " + fracPC + "%");
		}

	}


	protected abstract IEntity actuallyCreateEntity(AbstractGameClient client, NewEntityData msg);


	@Override
	public void messageReceived(MyAbstractMessage message) {
		if (Globals.DEBUG_MSGS) {
			Globals.p("Rcvd " + message.getClass().getSimpleName());
		}

		if (message instanceof PingMessage) {
			PingMessage pingMessage = (PingMessage) message;
			if (!pingMessage.s2c) {
				long rtt = System.currentTimeMillis() - pingMessage.originalSentTime;
				this.pingRTT = this.pingCalc.add(rtt);
				clientToServerDiffTime = pingMessage.responseSentTime - pingMessage.originalSentTime - (pingRTT/2); // If running on the same server, this should be 0! (or close enough)
			} else {
				pingMessage.responseSentTime = System.currentTimeMillis();
				networkClient.sendMessageToServer(message); // Send it straight back
			}
		} else {
			synchronized (unprocessedMessages) {
				unprocessedMessages.add(message);
			}
		}
	}


	protected void gameStatusChanged(int oldStatus, int newStatus) {
		// Override if required
	}


	@Override
	public void addEntity(IEntity e) {
		if (e.getID() == 0) {
			throw new RuntimeException("No entity id!");
		}
		if (e.getID() > 0 && e.getGameID() != this.getGameID()) {
			throw new RuntimeException("Entity " + e + " is for game " + e.getGameID() + ", current game is " + this.getGameID());
		}
		if (this.entities.containsKey(e.getID())) {
			throw new RuntimeException("Entity " + e + " already exists"); // Don't replace it, as entity has linked to other stuff, like Abilities
		}
		this.entities.put(e.getID(), e);
		if (e.requiresProcessing()) {
			this.entitiesForProcessing.add(e);
		}
		if (e instanceof PhysicalEntity) {
			entitiesToAddToRootNode.add((PhysicalEntity)e);
		}
		if (e.getID() == currentAvatarID && e != this.currentAvatar) {
			this.setAvatar(e);
		}
		if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
			if (e instanceof PhysicalEntity) {
				PhysicalEntity pe = (PhysicalEntity)e;
				Globals.p("Created " + pe + " at " + pe.getWorldTranslation());
			} else {
				Globals.p("Created " + e);
			}
		}
	}


	private void addEntityToGame(IEntity e) {
		PhysicalEntity pe = (PhysicalEntity)e;
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
			this.getGameNode().attachChild(pe.getMainNode());
		}
		pe.getMainNode().updateModelBound();

		if (pe.simpleRigidBody != null) {
			this.getPhysicsController().addSimpleRigidBody(pe.simpleRigidBody);
			if (Globals.STRICT) {
				if (this.physicsController.getNumEntities() > this.entities.size()) {
					Globals.pe("Warning: more simple rigid bodies than entities!");
				}
			}
		}

	}


	// Messing about with filters
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
		this.entitiesToAddToRootNode.clear();
		/*
		while (this.entities.size() > 0) {
			Iterator<IEntity> it = this.entities.values().iterator();
			IEntity e = it.next();
			e.remove();
			it.remove(); //this.entitiesToRemove
		}
		this.entitiesForProcessing.clear();
		 */
		for (IEntity e : this.entities.values()) {
			this.markForRemoval(e);
		}
		this.entityRemovalSystem.actuallyRemoveEntities();

		//this.clientOnlyEntities.clear(); // todo - remove() these
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
	public void markForRemoval(IEntity e) {
		if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
			Globals.p("Going to remove entity " + e.getID() + ":" + e);
		}
		this.entityRemovalSystem.markEntityForRemoval(e);
		// Remove physical entities from visuals immediately
		if (e instanceof PhysicalEntity) {
			PhysicalEntity pe = (PhysicalEntity)e;
			pe.getMainNode().removeFromParent();
		}
	}


	/*
	 * Note that an entity is responsible for clearing up it's own data!  This method should only remove the server's knowledge of the entity.  e.remove() does all the hard work.
	 */
	@Override
	public void actuallyRemoveEntity(int id) {
		IEntity e = this.entities.get(id);
		if (e != null) {
			if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
				Globals.p("Actually removing entity " + id + ":" + e);
			}
			this.entities.remove(id);
			if (e.requiresProcessing()) {
				this.entitiesForProcessing.remove(e);
			}
			e.remove();
			if (entitiesToAddToRootNode.contains(e)) {
				entitiesToAddToRootNode.remove(e); 
			}
			if (e == this.currentAvatar) {
				if (Globals.DEBUG_AVATAR_SET) {
					Globals.p("Avatar for player removed");
				}
				this.currentAvatar = null;
			}
			if (Globals.STRICT) {
				if (e instanceof PhysicalEntity) {
					PhysicalEntity pe = (PhysicalEntity)e;
					if (pe.getMainNode().getParent() != null) {
						Globals.pe("Entity still attached to rootNode!");
					}
				}
			}
		}
		//}
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
				quit("User quit.");
			}

		} else if (name.equalsIgnoreCase(TEST)) {
			if (value) {
				//new AbstractHUDImage(this, this.getNextEntityID(), this.hud, "Textures/text/winner.png", this.cam.getWidth(), this.cam.getHeight(), 5);
				//this.avatar.setWorldTranslation(new Vector3f(10, 10, 10));
			}
		}
	}


	public void quit(String reason) {
		Globals.p("quitting: " + reason);
		if (this.isConnected()) {
			if (playerID >= 0) {
				this.networkClient.sendMessageToServer(new PlayerLeftMessage(this.playerID));
			}
			this.networkClient.close();
		}
		this.stop();
		//System.exit(0); No, since we might be running multiple instances (e.g. Unit Tests)!
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
		joinedGame = false;
		this.playerID = -1;
		this.removeAllEntities();
	}


	@Override
	public SimplePhysicsController<PhysicalEntity> getPhysicsController() {
		return physicsController;
	}


	@Override
	public void collisionOccurred(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		PhysicalEntity pea = a.userObject;
		PhysicalEntity peb = b.userObject;

		this.collisionOccurred(pea, peb);
	}


	@Override
	public void collisionOccurred(PhysicalEntity pea, PhysicalEntity peb) {
		// Only do the simple stuff; the server handles anything more complex
		if (pea instanceof INotifiedOfCollision) {
			INotifiedOfCollision ic = (INotifiedOfCollision)pea;
			ic.notifiedOfCollision(peb);
		}
		if (peb instanceof INotifiedOfCollision) {
			INotifiedOfCollision ic = (INotifiedOfCollision)peb;
			ic.notifiedOfCollision(pea);
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
		this.consoleInput = s;
		//Globals.p("Received input: " + s);
	}


	private void checkConsoleInput() {
		try {
			if (this.consoleInput != null) {
				if (this.consoleInput.equalsIgnoreCase("help") || this.consoleInput.equalsIgnoreCase("?")) {
					Globals.p("stats, entities");
				} else if (this.consoleInput.equalsIgnoreCase("stats")) {
					showStats();
				} else if (this.consoleInput.equalsIgnoreCase("entities") || this.consoleInput.equalsIgnoreCase("e")) {
					listEntities();
				} else {
					Globals.p("Unknown command: " + this.consoleInput);
				}
				this.consoleInput = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	private void showStats() {
		Globals.p("Num Entities: " + this.entities.size());
		Globals.p("Num Entities for proc: " + this.entitiesForProcessing.size());
	}


	private void listEntities() {
		synchronized (entities) {
			// Do server-side first
			int count = 0;
			for (IEntity e : entities.values()) {
				if (e.getID() > 0) {
					Globals.p("Entity " + e.getID() + ": " + e.getName() + " (" + e + ")");
					count++;
				}
			}
			Globals.p("Total server entities:" + count);

			// Do client-side
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

	/*
	protected void setupFilters() {
		renderManager.setAlphaToCoverage(true);
		if (renderer.getCaps().contains(Caps.GLSL100)){
			fpp = new FilterPostProcessor(assetManager);
			//fpp.setNumSamples(4);
			int numSamples = getContext().getSettings().getSamples();
			if( numSamples > 0 ) {
				fpp.setNumSamples(numSamples); 
			}

			viewPort.addProcessor(fpp);
		}
	}
	 */

	@Override
	public SimpleGameData getGameData() {
		return this.gameData;
	}


	/**
	 * Override
	 * @param id
	 * @return
	 */
	protected String getSoundFileFromID(int id) {
		return null;
	}


	@Override
	public void playSound(int soundId, int entityId, Vector3f _pos, float _volume, boolean _stream) {
		//if (!Globals.MUTE) {
		String sound = getSoundFileFromID(soundId);
		if (sound != null && sound.length() > 0) {
			soundSystem.playSound(sound, entityId, _pos, _volume, _stream);
		}
		//}
	}


	public boolean isConnected() {
		return this.networkClient != null && networkClient.isConnected();
	}


	public void setPOVWeapon(IPOVWeapon weapon) {
		if (this.povWeapon != null) {
			this.povWeapon.hide();
		}
		this.povWeapon = weapon;
	}


	protected void avatarStarted() {
		if (povWeapon != null) {
			if (Globals.DEBUG_GUN_NOT_SHOWING) {
				Globals.p("Showing gun");
			}
			povWeapon.show(this.getGameNode());
		}
	}


	protected void avatarKilled() {
		if (povWeapon != null) {
			povWeapon.hide();
			if (Globals.DEBUG_GUN_NOT_SHOWING) {
				Globals.p("Hiding gun");
			}
		}
	}


	public void showOutlineEffect(Spatial model, int width, ColorRGBA color) {
		OutlineProFilter outlineFilter = model.getUserData("OutlineProFilter");
		if (outlineFilter == null) {
			ViewPort outlineViewport = renderManager.createPreView("outlineViewport", cam);
			FilterPostProcessor outlinefpp = new FilterPostProcessor(assetManager);
			OutlinePreFilter outlinePreFilter = new OutlinePreFilter();
			outlinefpp.addFilter(outlinePreFilter);
			outlineViewport.attachScene(model);
			outlineViewport.addProcessor(outlinefpp);

			outlineViewport.setClearFlags(true, false, false);
			outlineViewport.setBackgroundColor(new ColorRGBA(0f, 0f, 0f, 0f));

			outlineFilter = new OutlineProFilter(outlinePreFilter);
			model.setUserData("OutlineProFilter", outlineFilter);
			outlineFilter.setOutlineColor(color);
			outlineFilter.setOutlineWidth(width);
			fpp.addFilter(outlineFilter);
		} else {
			outlineFilter.setEnabled(true);
			outlineFilter.getOutlinePreFilter().setEnabled(true);
		}
	}


	public void hideOutlineEffect(Spatial model) {
		OutlineProFilter outlineFilter = model.getUserData("OutlineProFilter");
		if (outlineFilter != null) {
			outlineFilter.setEnabled(false);
			outlineFilter.getOutlinePreFilter().setEnabled(false);
		}
	}

}
