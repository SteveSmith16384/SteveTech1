package com.scs.stevetech1.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.scs.simplephysics.ICollisionListener;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.ICalcHitInPast;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IGetReadyForGame;
import com.scs.stevetech1.components.IPlayerControlled;
import com.scs.stevetech1.components.IProcessByServer;
import com.scs.stevetech1.components.IReloadable;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.components.ITargetable;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.data.SimplePlayerData;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractBullet;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.Entity;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.netmessages.AbilityActivatedMessage;
import com.scs.stevetech1.netmessages.ClientReloadRequestMessage;
import com.scs.stevetech1.netmessages.EntityUpdateMessage;
import com.scs.stevetech1.netmessages.GameLogMessage;
import com.scs.stevetech1.netmessages.GameOverMessage;
import com.scs.stevetech1.netmessages.GeneralCommandMessage;
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
import com.scs.stevetech1.networking.IGameMessageServer;
import com.scs.stevetech1.networking.IMessageServerListener;
import com.scs.stevetech1.networking.KryonetGameServer;
import com.scs.stevetech1.server.ClientData.ClientStatus;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.systems.EntityRemovalSystem;
import com.scs.stevetech1.systems.server.ServerGameStatusSystem;
import com.scs.stevetech1.systems.server.ServerPingSystem;

import ssmith.lang.Functions;
import ssmith.lang.NumberFunctions;
import ssmith.util.FixedLoopTime;
import ssmith.util.RealtimeInterval;
import ssmith.util.TextConsole;

/**
 *
 */
public abstract class AbstractGameServer extends SimpleApplication implements 
IEntityController, 
IMessageServerListener, // To listen for connecting game clients 
ICollisionListener<PhysicalEntity> {

	protected static AtomicInteger nextEntityID = new AtomicInteger(1);
	private static AtomicInteger nextGameID = new AtomicInteger(1);

	private RealtimeInterval checkGameStatusInterval = new RealtimeInterval(5000);

	// Systems
	protected ServerGameStatusSystem gameStatusSystem;
	private ServerPingSystem pingSystem;

	public HashMap<Integer, IEntity> entities = new HashMap<>(1000); // All entities todo - make protected
	public ArrayList<IEntity> entitiesForProcessing = new ArrayList<>(100); // Entities that we need to iterate over in game loop
	private EntityRemovalSystem entityRemovalSystem;

	protected SimplePhysicsController<PhysicalEntity> physicsController; // Checks all collisions
	protected FixedLoopTime loopTimer;  // Keep client and server running at the same time

	public IGameMessageServer gameNetworkServer;
	public ClientList clientList;

	private ServerSideCollisionLogic collisionLogic;
	private RealtimeInterval sendEntityUpdatesInterval;
	private List<MyAbstractMessage> unprocessedMessages = new LinkedList<>();
	public GameOptions gameOptions;
	private double commsVersion; // Check the client is up-to-date
	private String gameCode; // To prevent the wrong type of client connecting to the wrong type of server
	private boolean sendAddRemoveEntityMsgs = true;
	private String key;
	public boolean runningSlow = false;

	protected SimpleGameData gameData = new SimpleGameData();
	private ConsoleInputHandler consoleInput;

	/**
	 * 
	 * @param _gameCode  Must match the client's game code.
	 * @param _key Must match the server's game code.
	 * @param _gameOptions
	 * @param _tickrateMillis The interval between each iteration of the game loop.
	 * @param sendUpdateIntervalMillis How often to send updates to the clients
	 * @param _clientRenderDelayMillis How far in the past the client should render the game
	 * @param _timeoutMillis How long without comms before the server disconnects the client 
	 */
	public AbstractGameServer(String _gameCode, double _commsVersion, String _key, GameOptions _gameOptions) { 
		super();

		gameCode = _gameCode;
		commsVersion = _commsVersion;
		key = _key;
		gameOptions = _gameOptions;

		Globals.showWarnings();

		sendEntityUpdatesInterval = new RealtimeInterval(gameOptions.sendUpdateIntervalMillis);
		physicsController = new SimplePhysicsController<PhysicalEntity>(this, Globals.SUBNODE_SIZE);
		collisionLogic = new ServerSideCollisionLogic(this);
		loopTimer = new FixedLoopTime(gameOptions.tickrateMillis);
		this.entityRemovalSystem = new EntityRemovalSystem(this);

		setShowSettings(false); // Don't show settings dialog
		setPauseOnLostFocus(false);

		clientList = new ClientList(this);

		consoleInput = new ConsoleInputHandler(this); 
	}


	@Override
	public void simpleInitApp() {
		assetManager.registerLocator("assets/", FileLocator.class); // default
		assetManager.registerLocator("assets/", ClasspathLocator.class);

		this.gameStatusSystem = new ServerGameStatusSystem(this);
		this.pingSystem = new ServerPingSystem(this);

		// Start console
		new TextConsole(consoleInput);

		startNewGame(); // Even though there are no players connected, this created the gameData to avoid NPEs.

		startListeningForClients(); // Don't start listening until we're ready
		
		loopTimer.start();
	}

	
	private void startListeningForClients() {
		try {
			gameNetworkServer = new KryonetGameServer(gameOptions.ourExternalPort, gameOptions.ourExternalPort, this, gameOptions.timeoutMillis, getListofMessageClasses());
			Globals.p("Listening on port " + gameOptions.ourExternalPort);
		} catch (IOException e) {
			e.printStackTrace();
			this.stop();
		}


	}

	/**
	 *  
	 * @return a list of classes that must be registered in order to be sent from client to server or vice-versa.
	 */
	protected abstract Class[] getListofMessageClasses();


	@Override
	public void simpleUpdate(float tpfSecs) {
		if (tpfSecs > 1) {
			tpfSecs = 1;
		}

		consoleInput.checkConsoleInput(); // this.entitiesForProcessing;

		if (Globals.STRICT) {
			if (this.physicsController.getNumEntities() > this.entities.size()) {
				Globals.pe("Warning: more simple rigid bodies than entities!");
			}
			for(IEntity e : this.entities.values()) {
				if (e.requiresProcessing()) {
					if (!this.entitiesForProcessing.contains(e)) {
						Globals.p("Warning: Processed entity " + e + " not in process list!");
					}
				}
			}
		}

		this.clientList.addRemoveClients();

		if (gameNetworkServer.getNumClients() > 0) {
			handleMessages();
			iterateThroughClients();
			this.entityRemovalSystem.actuallyRemoveEntities();

			checkForRewinding();

			iterateThroughEntities(tpfSecs);

			if (checkGameStatusInterval.hitInterval() || this.gameData.getStatusEndTimeMS() < System.currentTimeMillis()) { // Try and catch it on zero-time remaining
				gameStatusSystem.checkGameStatus(false);
			}

			if (!Globals.DEBUG_MSGS) { // Don't send pings if we're debugging msgs
				this.pingSystem.process();
			}
		}

		this.runningSlow = loopTimer.waitForFinish() == false; // Keep clients and server running at same speed
		loopTimer.start();
	}


	private void handleMessages() {
		// Process all messages
		synchronized (unprocessedMessages) {
			while (!this.unprocessedMessages.isEmpty()) {
				MyAbstractMessage message = this.unprocessedMessages.remove(0);
				this.handleMessage(message);
			}
		}

	}


	private void iterateThroughClients() {
		for (ClientData client : this.clientList.getClients()) {
			if (!client.sentHello) {
				this.gameNetworkServer.sendMessageToClient(client, new HelloMessage());
				client.sentHello = true;
			}
		}
	}
	
	
	private void checkForRewinding() {
		// If any avatars are shooting a gun the requires "rewinding time", rewind all rewindable entities and calc the hits all together to save time
		boolean areAnyPlayersShooting = false;
		for (ClientData c : this.clientList.getClients()) {
			AbstractServerAvatar avatar = c.avatar;
			if (avatar != null && avatar.getAnyAbilitiesShootingInPast() != null) { //.isShooting() && avatar.abilityGun instanceof ICalcHitInPast) {
				areAnyPlayersShooting = true;
				break;
			}
		}
		if (areAnyPlayersShooting) {
			long timeTo = System.currentTimeMillis() - gameOptions.clientRenderDelayMillis; // Should this be by their ping time?
			this.rewindEntities(timeTo);
			this.rootNode.updateGeometricState();
			for (ClientData c : this.clientList.getClients()) {
				AbstractServerAvatar avatar = c.avatar;
				if (avatar != null) {
					ICalcHitInPast chip = avatar.getAnyAbilitiesShootingInPast();
					if (chip != null) {
						Vector3f from = avatar.getBulletStartPos();
						if (Globals.DEBUG_SHOOTING_POS) {
							Globals.p("Server shooting from " + from);
						}
						Ray ray = new Ray(from, avatar.getShootDir());
						ray.setLimit(chip.getRange());
						RayCollisionData rcd = avatar.checkForRayCollisions(ray);//, chip.getRange());
						if (rcd != null) {
							rcd.timestamp = timeTo; // For debugging
						}
						chip.setTarget(rcd); // Damage etc.. is calculated later
					}
				}
			}
			this.restoreEntityPositions();
		}

	}


	private void rewindEntities(long toTime) {
		for (IEntity e : this.entitiesForProcessing) {
			if (e instanceof IRewindable) {
				IRewindable r = (IRewindable)e;
				r.rewindPositionTo(toTime);
			}
		}
	}


	private void restoreEntityPositions() {
		for (IEntity e : entitiesForProcessing) {
			if (e instanceof IRewindable) {
				IRewindable r = (IRewindable)e;
				r.restorePosition();
			}
		}
	}


	private void iterateThroughEntities(float tpfSecs) {
		boolean sendUpdates = sendEntityUpdatesInterval.hitInterval();
		EntityUpdateMessage eum = null;
		if (sendUpdates) {
			eum = new EntityUpdateMessage();
		}

		// Loop through the entities
		for (int i=0 ; i<this.entitiesForProcessing.size() ; i++) {
			IEntity e = this.entitiesForProcessing.get(i);
			if (!e.isMarkedForRemoval()) {
				if (e instanceof IPlayerControlled) {
					IPlayerControlled p = (IPlayerControlled)e;
					p.resetPlayerInput();
				}

				if (e instanceof IProcessByServer) {
					IProcessByServer p = (IProcessByServer)e;
					p.processByServer(this, tpfSecs);
				}

				if (e instanceof PhysicalEntity) {
					PhysicalEntity physicalEntity = (PhysicalEntity)e;
					//strDebug.append(e.getID() + ": " + e.getName() + " Pos: " + physicalEntity.getWorldTranslation() + "\n");
					if (sendUpdates) {

						if (Globals.DEBUG_CPU_HUD_TEXT) {
							if (e.getName().equalsIgnoreCase("computer")) {
								Globals.p("Sending computer update");
							}
						}								

						if (physicalEntity.sendUpdates()) { // Don't send if not moved (unless player's Avatar)
							eum.addEntityData(physicalEntity, false, physicalEntity.createEntityUpdateDataRecord());
							physicalEntity.sendUpdate = false;
							if (eum.isFull()) {
								sendMessageToInGameClients(eum);
								eum = new EntityUpdateMessage(); // Start a new message
							}
						}
					}
				}
			}
		}

		if (sendUpdates) {
			sendMessageToInGameClients(eum);	
		}
	}


	protected void handleMessage(MyAbstractMessage message) {
		ClientData client = message.client;

		if (message instanceof JoinGameRequestMessage) {
			this.playerRequestToJoin(client, message);

		} else if (message instanceof PlayerLeftMessage) {
			this.connectionRemoved(client.getPlayerID());

		} else if (message instanceof PlayerInputMessage) { // Process these here so the inputs don't change values mid-thread
			PlayerInputMessage pim = (PlayerInputMessage)message;
			if (pim.timestamp > client.latestInputTimestamp) {
				client.remoteInput.decodeMessage(pim);
				client.latestInputTimestamp = pim.timestamp;
			}

		} else if (message instanceof AbilityActivatedMessage) {
			AbilityActivatedMessage elm = (AbilityActivatedMessage)message;
			AbstractServerAvatar shooter = (AbstractServerAvatar)this.entities.get(elm.avatarID);
			if (shooter != null) {
				IAbility ability = shooter.getAbility(elm.abilityID);
				ability.setToBeActivated(true);
			} else {
				Globals.p("Null shooter!");
			}

		} else if (message instanceof ClientReloadRequestMessage) {
			ClientReloadRequestMessage crm = (ClientReloadRequestMessage)message;
			IReloadable e = (IReloadable)this.entities.get(crm.abilityId);
			if (e != null) {
				e.setToBeReloaded();
			}

		} else {
			throw new RuntimeException("Unknown message type: " + message);
		}
	}


	protected void playerRequestToJoin(ClientData client, MyAbstractMessage message) {
		JoinGameRequestMessage newPlayerMessage = (JoinGameRequestMessage) message;
		if (!newPlayerMessage.gameCode.equalsIgnoreCase(gameCode)) {
			this.gameNetworkServer.sendMessageToClient(client, new JoinGameFailedMessage("Invalid Game code"));
			return;
		} else if (newPlayerMessage.clientVersion < this.commsVersion) {
			this.gameNetworkServer.sendMessageToClient(client, new JoinGameFailedMessage("Client too old; version " + commsVersion + " required"));
			return;
		} else if (!newPlayerMessage.key.equalsIgnoreCase(this.key)) {
			this.gameNetworkServer.sendMessageToClient(client, new JoinGameFailedMessage("Invalid key"));
			return;
		} else if (!this.doWeHaveSpaces()) {
			this.gameNetworkServer.sendMessageToClient(client, new JoinGameFailedMessage("No spaces available"));
			return;
		}

		client.clientStatus = ClientData.ClientStatus.InGame;
		client.playerData = this.createSimplePlayerData();
		client.playerData.id = client.id;
		client.playerData.playerName = newPlayerMessage.playerName;

		this.sendMessageToInGameClientsExcept(client, new ShowMessageMessage("Player joined game!", true));

		byte side = getSide(client);
		client.playerData.side = side;
		gameNetworkServer.sendMessageToClient(client, new GameSuccessfullyJoinedMessage(client.getPlayerID(), side)); // Must be before we send the avatar so they know it's their avatar
		sendGameStatusMessage(); // So they have a game ID, required when receiving ents
		client.avatar = createPlayersAvatar(client);
		sendAllEntitiesToClient(client);
		this.gameNetworkServer.sendMessageToClient(client, new SetAvatarMessage(client.getPlayerID(), client.avatar.getID()));
		client.avatar.startAgain();
		playerJoinedGame(client);
		appendToGameLog(client.playerData.playerName + " has joined the game");
		gameStatusSystem.checkGameStatus(true);
	}


	/**
	 * Override if you need a custom SimplePlayerData
	 */
	protected SimplePlayerData createSimplePlayerData() {
		return new SimplePlayerData();
	}


	public void sendGameStatusMessage() {
		ArrayList<SimplePlayerData> players = new ArrayList<SimplePlayerData>();
		for(ClientData client : this.clientList.getClients()) {
			if (client.clientStatus == ClientStatus.InGame) {
				players.add(client.playerData);
			}
		}
		if (players.size() > 0) {
			this.sendMessageToInGameClients(new SimpleGameDataMessage(this.gameData, players));
		}
	}


	public abstract boolean doWeHaveSpaces();


	protected void removeOldGame() {
		if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
			Globals.p("Removing all entities");
		}

		for (IEntity e : this.entities.values()) {
			this.markForRemoval(e.getID());
			//e.remove();
		}
		//this.actuallyRemoveEntities();
		this.entityRemovalSystem.actuallyRemoveEntities();
	}


	protected void startNewGame() {
		if (this.entities.size() > 0) {
			throw new RuntimeException("Outstanding entities");
		}
		if (this.entityRemovalSystem.getNumEntities() > 0) {
			throw new RuntimeException("Entities waiting to be removed");
		}

		if (this.getGameNode().getChildren().size() > 0) {
			Globals.p("Warning: There are still " + this.getGameNode().getChildren().size() + " children in the game node!  Forcing removal...");
			this.getGameNode().detachAllChildren();
		}
		if (this.getPhysicsController().getNumEntities() > 0) {
			Globals.p("Warning: There are still " + this.getPhysicsController().getNumEntities() + " children in the physics world!  Forcing removal...");
			this.getPhysicsController().removeAllEntities();
		}

		this.gameData.gameID = nextGameID.getAndAdd(1);
		sendGameStatusMessage(); // To send the new game ID

		Globals.p("------------------------------");
		Globals.p("Starting new game ID " + gameData.gameID);

		sendAddRemoveEntityMsgs = false; // Prevent sending new entity messages for all the entities - these will be sent further down
		this.createGame();
		sendAddRemoveEntityMsgs = true;

		// Create avatars and send new entities to players
		for (ClientData client : this.clientList.getClients()) {
			if (client.clientStatus == ClientData.ClientStatus.InGame) {
				byte side = getSide(client); // New sides
				client.playerData.side = side;
				client.avatar = createPlayersAvatar(client);
				sendAllEntitiesToClient(client);
				this.gameNetworkServer.sendMessageToClient(client, new SetAvatarMessage(client.getPlayerID(), client.avatar.getID()));
			}
		}

		this.gameStatusSystem.checkGameStatus(true); // Sets game status to "Deploying" if there's enough players

	}


	/**
	 * Determine the side for a player
	 * @param client
	 * @return The side
	 */
	public abstract byte getSide(ClientData client);


	protected abstract void createGame();


	protected void playerJoinedGame(ClientData client) {
		// Override if required
	}


	@Override
	public void messageReceived(int clientid, MyAbstractMessage message) {
		if (Globals.DEBUG_MSGS) {
			Globals.p("Rcvd " + message.getClass().getSimpleName());
		}

		ClientData client = null;
		synchronized (clientList) {
			client = clientList.getClient(clientid);
		}

		if (client == null) {
			Globals.p("Client unknown so msg '" + message.getClass().getSimpleName() + "' ignored");
			return;
		}

		MyAbstractMessage msg = (MyAbstractMessage)message;
		msg.client = client;

		if (message instanceof PingMessage) {
			PingMessage pingMessage = (PingMessage) message;
			this.pingSystem.handleMessage(pingMessage, client);
		} else {
			// Add it to list for processing in main thread
			synchronized (this.unprocessedMessages) {
				this.unprocessedMessages.add(msg);
			}
		}
	}


	private AbstractServerAvatar createPlayersAvatar(ClientData client) {
		int id = getNextEntityID();
		AbstractServerAvatar avatar = this.createPlayersAvatarEntity(client, id);
		avatar.startAgain(); // Must be before we add it, since that needs a position!
		this.actuallyAddEntity(avatar);
		return avatar;
	}


	public abstract void moveAvatarToStartPosition(AbstractAvatar avatar);


	protected abstract AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid);


	protected void sendAllEntitiesToClient(ClientData client) {
		NumEntitiesMessage numem = new NumEntitiesMessage(this.entities.size());
		this.gameNetworkServer.sendMessageToClient(client, numem);

		NewEntityMessage nem = new NewEntityMessage(this.getGameID());
		synchronized (entities) {
			for (IEntity e : entities.values()) {
				if (e.getGameID() == this.getGameID()) { // Since we might still have old entities that have not actually been removed!
					nem.add(e);
					if (nem.isFull()) {
						this.gameNetworkServer.sendMessageToClient(client, nem);
						if (Globals.SLEEP_BETWEEN_NEW_ENT_MSGS) {
							Functions.sleep(50); // Try to prevent buffer overflow
						}
						nem = new NewEntityMessage(this.getGameID());
					}
				}
			}
			this.gameNetworkServer.sendMessageToClient(client, nem);
		}
		GeneralCommandMessage aes = new GeneralCommandMessage(GeneralCommandMessage.Command.AllEntitiesSent);
		this.gameNetworkServer.sendMessageToClient(client, aes);
	}


	@Override
	public void connectionAdded(int id, Object net) {
		Globals.p("Client connected!");
		ClientData client = new ClientData(id, net);
		this.clientList.addClient(client);
		
		// Don't send anything straight away since they won't be on the client list yet
	}


	@Override
	public void connectionRemoved(int id) {
		Globals.p("connectionRemoved()");
		this.clientList.removeClient(id);
	}


	/**
	 * This gets called by the clientList, when we actually remove the client.
	 */
	protected void playerLeft(ClientData client) {
		Globals.p("Removing player " + client.getPlayerID());
		// Remove avatar
		if (client.avatar != null) {
			this.entityRemovalSystem.markEntityForRemoval(client.avatar);
		}

		if (client.playerData != null) {
			appendToGameLog(client.playerData.playerName + " has left");
		} else {
			Globals.pe("Client playerData is null!");
		}
		this.sendGameStatusMessage();
		gameStatusSystem.checkGameStatus(true);
	}


	@Override
	public void addEntity(IEntity e) {
		if (e == null) {
			throw new RuntimeException("Trying to add null entity");
		}
		this.actuallyAddEntity(e);
	}


	public void actuallyAddEntity(IEntity e) {
		synchronized (entities) {
			//Settings.p("Trying to add " + e + " (id " + e.getID() + ")");
			if (this.entities.containsKey(e.getID())) {
				throw new RuntimeException("Entity id " + e.getID() + " already exists: " + e);
			}
			this.entities.put(e.getID(), e);
			if (e.requiresProcessing()) {
				this.entitiesForProcessing.add(e);
			}
		}
		if (e instanceof PhysicalEntity) {
			PhysicalEntity pe = (PhysicalEntity)e;
			if (pe.getMainNode().getParent() != null) {
				throw new RuntimeException("Entity already has a node");
			}
			this.getGameNode().attachChild(pe.getMainNode());
			if (pe.simpleRigidBody != null) {
				this.getPhysicsController().addSimpleRigidBody(pe.simpleRigidBody);
			}
		}

		if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
			Globals.p("Created and added " + e);
		}

		// Tell clients
		if (sendAddRemoveEntityMsgs) {
			NewEntityMessage nem = new NewEntityMessage(this.getGameID());
			nem.add(e);
			for (ClientData client : this.clientList.getClients()) {
				if (client.clientStatus == ClientStatus.InGame) {
					gameNetworkServer.sendMessageToClient(client, nem);
				}
			}
		}
	}


	@Override
	public void markForRemoval(int id) {
		Entity e = (Entity)this.entities.get(id);
		this.entityRemovalSystem.markEntityForRemoval(e);
	}


	/*
	 * Note that an entity is responsible for clearing up it's own data!  This method should only remove the server's knowledge of the entity.  e.remove() does all the hard work.
	 */
	@Override
	public void actuallyRemoveEntity(int id) {
		IEntity e = this.entities.get(id);
		if (e != null) {
			if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
				Globals.p("Actually removing entity " + e + (this.sendAddRemoveEntityMsgs ? " ..and sending message to clients" : ""));
			}
			this.entities.remove(id);
			if (e.requiresProcessing()) {
				this.entitiesForProcessing.remove(e);
			}
			e.remove();
		}
		if (sendAddRemoveEntityMsgs) {
			if (e instanceof AbstractBullet == false) { // Don't send remove for these as the client takes care of it
				this.sendMessageToInGameClients(new RemoveEntityMessage(id));
			}
		}
	}


	@Override
	public boolean isServer() {
		return true;
	}


	@Override
	public int getNextEntityID() {
		return nextEntityID.getAndAdd(1);
	}


	@Override
	public void collisionOccurred(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		PhysicalEntity pea = a.userObject;
		PhysicalEntity peb = b.userObject;

		this.collisionOccurred(pea, peb);
	}


	@Override
	public void collisionOccurred(PhysicalEntity pea, PhysicalEntity peb) {
		collisionLogic.collision(pea, peb);
	}


	@Override
	public SimplePhysicsController<PhysicalEntity> getPhysicsController() {
		return physicsController;
	}


	public ITargetable getTarget(PhysicalEntity shooter, byte ourSide, float range, float viewAngleRads) {
		ITargetable target = null;
		int highestPri = -1;
		float closestDist = 9999f;

		for (IEntity e : entitiesForProcessing) {
			if (e != shooter) {
				if (e instanceof ITargetable) {
					ITargetable t = (ITargetable)e;
					if (t.getTargetPriority() >= highestPri) {
						if (t.isAlive() && t.isValidTargetForSide(ourSide)) {
							PhysicalEntity pe = (PhysicalEntity)e;
							float dist = shooter.distance(pe);
							if (t.getTargetPriority() > highestPri || dist < closestDist) {
								if (shooter.canSee(pe, range, viewAngleRads)) {
									target = t;
									highestPri = t.getTargetPriority();
									closestDist = dist;
								}
							}
						}
					}
				}
			}
		}
		return target; 
	}


	@Override
	public int getGameID() {
		return this.gameData.gameID;
	}


	public void gameStatusChanged(int newStatus) {
		if (newStatus == SimpleGameData.ST_DEPLOYING) {
			sendMessageToInGameClients(new GeneralCommandMessage(GeneralCommandMessage.Command.GameRestarting));
			sendMessageToInGameClients(new GeneralCommandMessage(GeneralCommandMessage.Command.RemoveAllEntities)); // Before we increment the game id!

			sendAddRemoveEntityMsgs = false; // Prevent sending "remove entities" messages for all the entities
			removeOldGame();
			sendAddRemoveEntityMsgs = true;

			startNewGame();
			this.appendToGameLog("Get ready!");

		} else if (newStatus == SimpleGameData.ST_STARTED) {
			synchronized (entities) {
				for (IEntity e : entities.values()) {
					if (e instanceof IGetReadyForGame) {
						IGetReadyForGame grfg = (IGetReadyForGame)e;
						grfg.getReadyForGame();
					}
				}
			}
			this.appendToGameLog("Game Started!");

		} else if (newStatus == SimpleGameData.ST_FINISHED) {
			this.appendToGameLog("Game Finished!");

			byte winningSide = this.getWinningSideAtEnd();
			//String name = getSideName(winningSide);
			//this.appendToGameLog(name + " has won!");
			this.sendMessageToInGameClients(new GameOverMessage(winningSide));
		}
	}


	protected abstract byte getWinningSideAtEnd();


	public abstract int getMinPlayersRequiredForGame();


	@Override
	public synchronized int getNumEntities() {
		return this.entities.size();
	}


	public void handleCommand(String cmd) {
		if (cmd.equals("warp")) {
			for(ClientData client : this.clientList.getClients()) {
				if (client.avatar != null) {
					Globals.p("Warping player");
					client.avatar.setWorldTranslation(new Vector3f(10, 10, 10));
					break;
				}
			}
		} else if (cmd.equals("quit")) {
			this.gameNetworkServer.close();
			this.stop();
		}
	}


	@Override
	public Node getGameNode() {
		return rootNode;
	}


	public void playerKilled(AbstractServerAvatar avatar) {
		// Override if req
	}


	public SimpleGameData getGameData() {
		return this.gameData;
	}


	@Override
	public boolean canCollide(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		PhysicalEntity pa = a.userObject; //pa.getMainNode().getWorldBound();
		PhysicalEntity pb = b.userObject; //pb.getMainNode().getWorldBound();

		return canCollide(pa, pb);
	}


	public void sendBulletTrail(int playerID, Vector3f start, Vector3f end) {
		NewEntityMessage nem = new NewEntityMessage(this.getGameID());

		NewEntityData data = new NewEntityData();
		data.type = Globals.BULLET_TRAIL;
		data.data.put("playerID", playerID);
		data.data.put("start", start);
		data.data.put("end", end);

		sendMessageToInGameClients(nem);

	}


	public void sendExplosion(Vector3f pos, int num, float minForce, float maxForce, float minSize, float maxSize, String tex) {
		NewEntityMessage nem = new NewEntityMessage(this.getGameID());

		for (int i=0 ; i<num ; i++) {
			Vector3f forceDirection = new Vector3f(NumberFunctions.rndFloat(-1, 1), NumberFunctions.rndFloat(1, 1.1f), NumberFunctions.rndFloat(-1, 1));
			forceDirection.multLocal(NumberFunctions.rndFloat(minForce,  maxForce));
			float size = NumberFunctions.rndFloat(minSize,  maxSize);

			NewEntityData data = new NewEntityData();
			data.type = Globals.EXPLOSION_SHARD;
			data.data.put("pos", pos);//this.getWorldTranslation());
			data.data.put("forceDirection", forceDirection);
			data.data.put("size", size);
			data.data.put("tex", tex);
			nem.data.add(data);
		}

		sendMessageToInGameClients(nem);

	}


	public void sendExpandingSphere(Vector3f pos) {
		NewEntityMessage nem = new NewEntityMessage(this.getGameID());

		NewEntityData data = new NewEntityData();
		data.type = Globals.EXPLOSION_SPHERE;
		data.data.put("pos", pos);
		nem.data.add(data);

		sendMessageToInGameClients(nem);

	}


	public void moveEntityUntilItHitsSomething(PhysicalEntity pe, Vector3f dir) {
		this.moveEntityUntilItHitsSomething(pe, dir, 1f);
		this.moveEntityUntilItHitsSomething(pe, dir, 0.01f); // Was 0.1f, but spacecrates were floating
	}


	public void moveEntityUntilItHitsSomething(PhysicalEntity pe, Vector3f dir, float offset) {
		Vector3f voffset = dir.mult(offset);
		while (pe.simpleRigidBody.checkForCollisions(false).isEmpty()) {
			pe.getMainNode().move(voffset);
		}
		pe.getMainNode().move(dir.mult(-offset)); // Move back
	}


	public void appendToGameLog(String s) {
		this.sendMessageToInGameClients(new GameLogMessage(s));
	}


	public void damageSurroundingEntities(PhysicalEntity exploder, float range, float damage) {
		Vector3f pos = exploder.getMainNode().getWorldBound().getCenter();
		List<SimpleRigidBody<PhysicalEntity>> list = this.physicsController.getSRBsWithinRange(pos, range);
		for (SimpleRigidBody<PhysicalEntity> srb : list) {
			PhysicalEntity pe = (PhysicalEntity)srb.simpleEntity;
			if (pe != exploder) { // DOn't damage ourselves (we'll get caught in a loopprobably)
				if (pe instanceof IDamagable) {
					if (pe.canSee(exploder, range, -1f)) {
						IDamagable id = (IDamagable)pe;
						id.damaged(damage, null, "Explosion");
						Globals.p(pe + " was damaged " + damage + " by explosion");
					}
				}
			}
		}

	}


	@Override
	public void playSound(int _soundId, int entityId, Vector3f _pos, float _volume, boolean _stream) {
		sendMessageToInGameClients(new PlaySoundMessage(_soundId, entityId, _pos, _volume, _stream));

	}


	public void sendMessageToInGameClients(MyAbstractMessage msg) {
		this.sendMessageToInGameClientsExcept(null, msg);
	}


	/*
	 * This will send a message to all connected clients, whether they've joined the game or not.
	 * Are you sure you need to call this method?
	 */
	public void sendMessageToAll_AreYouSure(MyAbstractMessage msg) {
		for(ClientData client : this.clientList.getClients()) {
			this.gameNetworkServer.sendMessageToClient(client, msg);
		}
	}


	public void sendMessageToInGameClientsExcept(ClientData ex, MyAbstractMessage msg) {
		for(ClientData client : this.clientList.getClients()) {
			if (client != ex) {
				if (client.clientStatus == ClientStatus.InGame) {
					this.gameNetworkServer.sendMessageToClient(client, msg);
				}
			}
		}
	}

}
