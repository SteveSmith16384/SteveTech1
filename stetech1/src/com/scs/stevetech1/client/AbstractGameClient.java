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

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
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
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.hud.IHUD;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.input.MouseAndKeyboardCamera;
import com.scs.stevetech1.lobby.KryonetLobbyClient;
import com.scs.stevetech1.netmessages.AbilityUpdateMessage;
import com.scs.stevetech1.netmessages.AvatarStartedMessage;
import com.scs.stevetech1.netmessages.AvatarStatusMessage;
import com.scs.stevetech1.netmessages.EntityKilledMessage;
import com.scs.stevetech1.netmessages.EntityLaunchedMessage;
import com.scs.stevetech1.netmessages.EntityUpdateData;
import com.scs.stevetech1.netmessages.EntityUpdateMessage;
import com.scs.stevetech1.netmessages.GameOverMessage;
import com.scs.stevetech1.netmessages.GameSuccessfullyJoinedMessage;
import com.scs.stevetech1.netmessages.GeneralCommandMessage;
import com.scs.stevetech1.netmessages.GenericStringMessage;
import com.scs.stevetech1.netmessages.JoinGameFailedMessage;
import com.scs.stevetech1.netmessages.ModelBoundsMessage;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.netmessages.NewEntityMessage;
import com.scs.stevetech1.netmessages.NewPlayerRequestMessage;
import com.scs.stevetech1.netmessages.PingMessage;
import com.scs.stevetech1.netmessages.PlaySoundMessage;
import com.scs.stevetech1.netmessages.PlayerInputMessage;
import com.scs.stevetech1.netmessages.PlayerLeftMessage;
import com.scs.stevetech1.netmessages.RemoveEntityMessage;
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

import ssmith.util.AverageNumberCalculator;
import ssmith.util.FixedLoopTime;
import ssmith.util.RealtimeInterval;

public abstract class AbstractGameClient extends SimpleApplication implements IClientApp, IEntityController, ActionListener, IMessageClientListener, ICollisionListener<PhysicalEntity> { 

	// Statuses
	public static final int STATUS_NOT_CONNECTED = 0;
	public static final int STATUS_CONNECTED_TO_LOBBY = 1;
	public static final int STATUS_CONNECTED_TO_GAME = 2;
	public static final int STATUS_RCVD_WELCOME = 3;
	public static final int STATUS_SENT_JOIN_REQUEST = 4;
	public static final int STATUS_JOINED_GAME = 5; // About to be sent all the entities
	public static final int STATUS_STARTED = 6; // Have received all entities

	private static final String JME_SETTINGS_NAME = "jme_client_settings.txt";

	// Global controls
	private static final String QUIT = "Quit";
	protected static final String TEST = "Test";

	protected static AtomicInteger nextEntityID = new AtomicInteger(1);

	public HashMap<Integer, IEntity> entities = new HashMap<>(100);
	protected HashMap<Integer, IEntity> entitiesForProcessing = new HashMap<>(100); // Entites that we need to iterate over in game loop
	protected LinkedList<IEntity> entitiesToAdd = new LinkedList<IEntity>();
	protected LinkedList<Integer> entitiesToRemove = new LinkedList<Integer>();

	protected SimplePhysicsController<PhysicalEntity> physicsController; // Checks all collisions
	protected FixedLoopTime loopTimer;  // Keep client and server running at the same time

	public int tickrateMillis, clientRenderDelayMillis, timeoutMillis;

	private RealtimeInterval sendPingInterval = new RealtimeInterval(Globals.PING_INTERVAL_MS);

	private HashMap<Integer, IEntity> clientOnlyEntities = new HashMap<>(100);
	private List<IEntity> clientOnlyEntitiesToAdd = new LinkedList<IEntity>();
	private List<Integer> clientOnlyEntitiesToRemove = new LinkedList<Integer>();

	private String gameID;
	private String playerName = "[Player's Name]";
	//private KryonetLobbyClient lobbyClient;
	public IGameMessageClient networkClient;
	public IHUD hud;
	public IInputDevice input;
	private Node playersWeaponNode;

	public AbstractClientAvatar currentAvatar;
	public int playerID = -1;
	public int side = -1;
	public int score;
	private AverageNumberCalculator pingCalc = new AverageNumberCalculator(4);
	public long pingRTT;
	private long clientToServerDiffTime; // Add to current time to get server time
	public int clientStatus = STATUS_NOT_CONNECTED;
	public SimpleGameData gameData;
	public ArrayList<SimplePlayerData> playersList;

	protected Node gameNode = new Node("GameNode");
	protected Node debugNode = new Node("DebugNode");

	private List<MyAbstractMessage> unprocessedMessages = new LinkedList<>();

	public long serverTime, renderTime;
	private String gameServerIP;//, lobbyIP;
	private int gamePort;//, lobbyPort;
	private float mouseSens;

	// Entity systems
	private AnimationSystem animSystem;
	private ClientEntityLauncherSystem launchSystem;

	protected AbstractGameClient(String _gameID, String appTitle, String logoImage, String _gameServerIP, int _gamePort, //String _lobbyIP, int _lobbyPort, 
			int _tickrateMillis, int _clientRenderDelayMillis, int _timeoutMillis, float _mouseSens) { // float gravity, float aerodynamicness, 
		super();

		gameID = _gameID;

		tickrateMillis = _tickrateMillis;
		clientRenderDelayMillis = _clientRenderDelayMillis;
		timeoutMillis = _timeoutMillis;

		loopTimer = new FixedLoopTime(tickrateMillis);

		gameServerIP = _gameServerIP;
		gamePort = _gamePort;
		//lobbyIP = _lobbyIP;
		//lobbyPort = _lobbyPort;

		physicsController = new SimplePhysicsController<PhysicalEntity>(this, 15); // todo - get 15,1 params from server?
		animSystem = new AnimationSystem(this);
		launchSystem = new ClientEntityLauncherSystem(this);

		mouseSens = _mouseSens;

		settings = new AppSettings(true);
		try {
			settings.load(JME_SETTINGS_NAME);
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		settings.setUseJoysticks(true);
		//settings.setAudioRenderer(null); // Avoid error with no soundcard
		settings.setTitle(Globals.HIDE_BELLS_WHISTLES ? "Client" : appTitle);// + " (v" + Settings.VERSION + ")");
		settings.setSettingsDialogImage(logoImage);

		setSettings(settings);
		setPauseOnLostFocus(false); // Needs to always be in sync with server!

		start();

		try {
			settings.save(JME_SETTINGS_NAME);
		} catch (BackingStoreException e) {
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

		cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.01f, Globals.CAM_DIST);

		getInputManager().addMapping(QUIT, new KeyTrigger(KeyInput.KEY_ESCAPE));
		getInputManager().addListener(this, QUIT);            

		getInputManager().addMapping(TEST, new KeyTrigger(KeyInput.KEY_T));
		getInputManager().addListener(this, TEST);            

		setUpLight();

		hud = this.getHUD();
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

		loopTimer.start();

	}

	protected abstract Class[] getListofMessageClasses();

	protected abstract IHUD getHUD();

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
		if (Globals.STRICT) {
			if (this.physicsController.getEntities().size() > this.entities.size()) {
				Globals.pe("Warning: more simple rigid bodies than entities!");
			}
		}
		
		if (tpf_secs > 1) { 
			tpf_secs = 1;
		}


		try {
			serverTime = System.currentTimeMillis() + this.clientToServerDiffTime;
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

				if (clientStatus == STATUS_STARTED) {

					this.sendInputs();

					if (Globals.SHOW_LATEST_AVATAR_POS_DATA_TIMESTAMP) {
						try {
							long timeDiff = this.currentAvatar.historicalPositionData.getMostRecent().serverTimestamp - renderTime;
							this.hud.setDebugText("Latest Data is " + timeDiff + " newer than we need");
						} catch (Exception ex) {
							// do nothing, no data yet
						}
					}

					// Add entities
					Iterator<IEntity> it = this.entitiesToAdd.iterator();
					while (it.hasNext()) {
						IEntity e = it.next();
						this.actuallyAddEntity(e);
					}
					it = null;
					this.entitiesToAdd.clear();

					// Remove entities
					Iterator<Integer> it2 = this.entitiesToRemove.iterator();
					while (it2.hasNext()) {
						int i = it2.next();
						this.actuallyRemoveEntity(i);
					}
					it2 = null;
					this.entitiesToRemove.clear();

					// Add client-only entities
					Iterator<IEntity> coit = this.clientOnlyEntitiesToAdd.iterator();
					while (coit.hasNext()) {
						IEntity e = coit.next();
						this.clientOnlyEntities.put(e.getID(), e);
					}
					coit = null;
					this.clientOnlyEntitiesToAdd.clear();

					// Remove entities
					Iterator<Integer> coit2 = this.clientOnlyEntitiesToRemove.iterator();
					while (coit2.hasNext()) {
						int i = coit2.next();
						this.clientOnlyEntities.remove(i);
					}
					coit2 = null;
					this.clientOnlyEntitiesToRemove.clear();

					this.launchSystem.process(renderTime);

					// Loop through each entity and process them
					for (IEntity e : entitiesForProcessing.values()) {
						if (e instanceof IPlayerControlled) {
							IPlayerControlled p = (IPlayerControlled)e;
							p.resetPlayerInput();
						}
						if (e instanceof PhysicalEntity) {
							PhysicalEntity pe = (PhysicalEntity)e;
							
							pe.calcPosition(renderTime, tpf_secs); // Must be before we process physics as this calcs additionalForce
							pe.processChronoData(renderTime, tpf_secs);

							if (Globals.STRICT) {
								if (e instanceof AbstractClientAvatar == false && e instanceof IClientControlled == false) {
									if (pe.simpleRigidBody != null) {
										if (pe.simpleRigidBody.movedByForces()) {
											Globals.p("Warning: client-side entity not kinematic!");
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

					// Now do client-only entities
					for (IEntity e : this.clientOnlyEntities.values()) {
						if (e instanceof IProcessByClient) {
							IProcessByClient pbc = (IProcessByClient)e;
							pbc.processByClient(this, tpf_secs);
						}
					}

					//this.hud.log_ta.setText(strListEnts.toString());
				}

				// Show players gun
				if (playersWeaponNode != null) {
					playersWeaponNode.setLocalTranslation(cam.getLocation());
					playersWeaponNode.lookAt(cam.getLocation().add(cam.getDirection()), Vector3f.UNIT_Y);
					//playersWeaponNode.updateGeometricState();  // Makes the gun black for some reason
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

		} else if (message instanceof GenericStringMessage) {
			GenericStringMessage gsm = (GenericStringMessage)message;
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
				networkClient.sendMessageToServer(new NewPlayerRequestMessage(gameID, playerName));
				clientStatus = STATUS_SENT_JOIN_REQUEST;
			} else {
				throw new RuntimeException("Received second welcome message");
			}

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
			//if (!this.entities.containsKey(newEntityMessage.entityID)) {
			createEntity(newEntityMessage, newEntityMessage.timestamp);
			/*} else {
				// We already know about it. -  NO! Replace the entity!
			}*/

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
						if (Globals.DEBUG_NO_BULLET && pe.type == 7) { // Laser bullet
							Globals.p("Received EntityUpdateMessage for " + e);
						}
						pe.storePositionData(eud, mainmsg.timestamp);
						pe.chronoUpdateData.addData(eud);
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
			IEntity e = this.entities.get(rem.entityID);
			if (e != null) {
				e.remove();
			}
			//this.removeEntity(rem.entityID);

		} else if (message instanceof GeneralCommandMessage) {
			GeneralCommandMessage msg = (GeneralCommandMessage)message;
			if (msg.command == GeneralCommandMessage.Command.AllEntitiesSent) { // We now have enough data to start
				clientStatus = STATUS_STARTED;
				this.getRootNode().attachChild(this.gameNode);
				this.showPlayersWeapon();
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

		} else if (message instanceof EntityLaunchedMessage) {
			if (Globals.DEBUG_SERVER_SHOOTING) {
				Globals.p("Received EntityLaunchedMessage");
			}
			EntityLaunchedMessage elm = (EntityLaunchedMessage)message;
			this.launchSystem.scheduleLaunch(elm); //this.entities

		} else if (message instanceof AvatarStartedMessage) {
			if (Globals.DEBUG_PLAYER_RESTART) {
				Globals.p("Rcvd AvatarStartedMessage");
			}
			AvatarStartedMessage asm = (AvatarStartedMessage)message;
			if (this.currentAvatar != null && asm.entityID == this.currentAvatar.getID()) {
				AbstractAvatar avatar = (AbstractAvatar)this.entities.get(asm.entityID);
				avatar.setAlive(true); 
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
				this.score = asm.score;
				this.currentAvatar.moveSpeed = asm.moveSpeed;
				this.currentAvatar.setJumpForce(asm.jumpForce);
				if (asm.damaged) {
					hud.showDamageBox();
				}
			}

		} else if (message instanceof GameOverMessage) {
			GameOverMessage gom = (GameOverMessage)message;
			if (gom.winningSide == -1) {
				Globals.p("The game is a draw!");
				this.gameIsDrawn();
			} else if (gom.winningSide == this.side) {
				Globals.p("You have won!");
				this.playerHasWon();
			} else {
				Globals.p("You have lost!");
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

		} else {
			throw new RuntimeException("Unknown message type: " + message);
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
		try {
			AudioNode node = new AudioNode(this.getAssetManager(), psm.sound, psm.stream ?  AudioData.DataType.Stream : AudioData.DataType.Buffer);
			node.setLocalTranslation(psm.pos);
			node.setVolume(psm.volume);
			node.setLooping(false);
			node.play();

			this.gameNode.attachChild(node);

			// Create thread to remove it
			this.enqueue(new Callable<Spatial>() {
				public Spatial call() throws Exception {
					try {
						Thread.sleep((long)node.getPlaybackTime());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					node.removeFromParent();
					return node;
				}
			});

		} catch (Exception ex) {
			ex.printStackTrace();
		}
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


	protected final void createEntity(NewEntityMessage msg, long timeToCreate) {
		IEntity e = actuallyCreateEntity(this, msg);
		if (e != null) {
			if (e instanceof AbstractAvatar || e instanceof IAbility || e instanceof AbstractEnemyAvatar) {
				this.actuallyAddEntity(e); // Need to add it immediately so there's an avatar to add the grenade launcher to, or a grenade launcher to add a bullet to
				// todo -point camera here
			} else {
				this.addEntity(e); // Schedule it for addition at the right time
			}
		}

	}


	protected abstract IEntity actuallyCreateEntity(AbstractGameClient client, NewEntityMessage msg);


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
		if (e.getID() <= 0) {
			throw new RuntimeException("No entity id!");
		}
		this.entitiesToAdd.add(e);
	}


	private void actuallyAddEntity(IEntity e) {
		synchronized (entities) {
			if (e.getID() <= 0) {
				throw new RuntimeException("No entity id!");
			}
			if (this.entities.containsKey(e.getID())) {
				//throw new RuntimeException("Entity " + e.getID() + " already exists");
				e.remove();
				this.actuallyRemoveEntity(e.getID()); // Replace it, since it might be an existing entity but its position has changed
			}
			this.entities.put(e.getID(), e);
			if (e.requiresProcessing()) {
				this.entitiesForProcessing.put(e.getID(), e);
			}

			if (e instanceof PhysicalEntity) {
				boolean add = true;
				if (e instanceof ILaunchable) { // Don't add bullets until they are fired!
					ILaunchable il = (ILaunchable)e;
					add = il.hasBeenLaunched();
				}
				if (add) {
					PhysicalEntity pe = (PhysicalEntity)e;
					this.getGameNode().attachChild(pe.getMainNode());
					if (pe.simpleRigidBody != null) {
						this.getPhysicsController().addSimpleRigidBody(pe.simpleRigidBody);
					}
				}
			}
			if (e instanceof IDrawOnHUD) {
				IDrawOnHUD doh = (IDrawOnHUD)e;
				this.hud.addItem(doh.getHUDItem());
			}

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


	@Override
	public void removeEntity(int id) {
		this.entitiesToRemove.add(id);
	}


	/*
	 * Note that an entity is responsible for clearing up it's own data!  This method should only remove the server's knowledge of the entity.  e.remove() does all the hard work.
	 */
	private void actuallyRemoveEntity(int id) {
		synchronized (entities) {
			IEntity e = this.entities.get(id);
			if (e != null) {
				if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
					Globals.p("Actually removing entity " + id + ":" + e.getName());
				}
				/*if (e instanceof PhysicalEntity) {
					PhysicalEntity pe =(PhysicalEntity)e;
					if (pe.simpleRigidBody != null) {
						this.physicsController.removeSimpleRigidBody(pe.simpleRigidBody);
					}
					pe.getMainNode().removeFromParent();
				}*/
				/*if (e instanceof IDrawOnHUD) {
					IDrawOnHUD doh = (IDrawOnHUD)e;
					doh.getHUDItem().removeFromParent();
				}*/
				this.entities.remove(id);
				if (e.requiresProcessing()) {
					this.entitiesForProcessing.remove(id);
				}
			} else {
				Globals.pe("Entity id " + id + " not found for removal");
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
				//new AbstractHUDImage(this, this.getNextEntityID(), this.hud, "Textures/text/winner.png", this.cam.getWidth(), this.cam.getHeight(), 5);
				//this.avatar.setWorldTranslation(new Vector3f(10, 10, 10));
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
		System.exit(0);
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


	//@Override
	public void addClientOnlyEntity(IEntity e) {
		this.clientOnlyEntitiesToAdd.add(e);
	}


	public void removeClientOnlyEntity(IEntity e) {
		this.clientOnlyEntitiesToRemove.add(e.getID());
	}


	@Override
	public int getNextEntityID() {
		return nextEntityID.getAndAdd(1);
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
		Spatial s = getPlayersWeaponModel();
		if (s != null) {
			playersWeaponNode.attachChild(s);
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



}
