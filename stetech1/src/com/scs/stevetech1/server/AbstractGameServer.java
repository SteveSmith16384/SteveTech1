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
import com.jme3.system.JmeContext;
import com.scs.simplephysics.ICollisionListener;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.ICalcHitInPast;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IGetReadyForGame;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.components.IPlayerControlled;
import com.scs.stevetech1.components.IProcessByServer;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.components.ITargetable;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.data.SimplePlayerData;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.netmessages.AbilityActivatedMessage;
import com.scs.stevetech1.netmessages.EntityUpdateMessage;
import com.scs.stevetech1.netmessages.GameOverMessage;
import com.scs.stevetech1.netmessages.GameSuccessfullyJoinedMessage;
import com.scs.stevetech1.netmessages.GeneralCommandMessage;
import com.scs.stevetech1.netmessages.GenericStringMessage;
import com.scs.stevetech1.netmessages.JoinGameFailedMessage;
import com.scs.stevetech1.netmessages.ModelBoundsMessage;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.netmessages.NewEntityData;
import com.scs.stevetech1.netmessages.NewEntityMessage;
import com.scs.stevetech1.netmessages.NewPlayerRequestMessage;
import com.scs.stevetech1.netmessages.PingMessage;
import com.scs.stevetech1.netmessages.PlayerInputMessage;
import com.scs.stevetech1.netmessages.PlayerLeftMessage;
import com.scs.stevetech1.netmessages.RemoveEntityMessage;
import com.scs.stevetech1.netmessages.SetAvatarMessage;
import com.scs.stevetech1.netmessages.SimpleGameDataMessage;
import com.scs.stevetech1.netmessages.WelcomeClientMessage;
import com.scs.stevetech1.networking.IGameMessageServer;
import com.scs.stevetech1.networking.IMessageClientListener;
import com.scs.stevetech1.networking.IMessageServerListener;
import com.scs.stevetech1.networking.KryonetGameServer;
import com.scs.stevetech1.server.ClientData.ClientStatus;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.systems.server.ServerGameStatusSystem;
import com.scs.stevetech1.systems.server.ServerPingSystem;

import ssmith.lang.Functions;
import ssmith.util.ConsoleInputListener;
import ssmith.util.FixedLoopTime;
import ssmith.util.RealtimeInterval;
import ssmith.util.TextConsole;

/**
 * This is a bare-bones entity server for controlling entites over a network.
 * It has no concept of game state.
 *
 */
public abstract class AbstractGameServer extends SimpleApplication implements 
IEntityController, 
IMessageServerListener, // To listen for connecting game clients 
IMessageClientListener, // For sending messages to the lobby server
ICollisionListener<PhysicalEntity>,
ConsoleInputListener {

	protected static AtomicInteger nextEntityID = new AtomicInteger(1);
	private static AtomicInteger nextGameID = new AtomicInteger(1);

	//private KryonetLobbyClient clientToLobbyServer;
	//private RealtimeInterval updateLobbyInterval = new RealtimeInterval(30 * 1000);

	private RealtimeInterval checkGameStatusInterval = new RealtimeInterval(5000);

	// Systems
	private ServerGameStatusSystem gameStatusSystem;
	private ServerPingSystem pingSystem;

	protected HashMap<Integer, IEntity> entities = new HashMap<>(100); // All entities
	//protected HashMap<Integer, IEntity> entitiesForProcessing = new HashMap<>(100); // Entites that we need to iterate over in game loop
	protected ArrayList<IEntity> entitiesForProcessing = new ArrayList<>(10); // Entites that we need to iterate over in game loop
	//protected LinkedList<IEntity> entitiesToAdd = new LinkedList<IEntity>();
	protected LinkedList<Integer> entitiesToRemove = new LinkedList<Integer>();

	protected SimplePhysicsController<PhysicalEntity> physicsController; // Checks all collisions
	protected FixedLoopTime loopTimer;  // Keep client and server running at the same time

	public int tickrateMillis, clientRenderDelayMillis, timeoutMillis;

	public IGameMessageServer gameNetworkServer;
	public HashMap<Integer, ClientData> clients = new HashMap<>(10); // PlayerID::ClientData
	private LinkedList<ClientData> clientsToAdd = new LinkedList<>();
	private LinkedList<Integer> clientsToRemove = new LinkedList<>();

	public ServerSideCollisionLogic collisionLogic = new ServerSideCollisionLogic();
	private RealtimeInterval sendEntityUpdatesInterval;
	private List<MyAbstractMessage> unprocessedMessages = new LinkedList<>();
	public GameOptions gameOptions;
	private String gameCode; // To prevent the wrong type of client connecting to the wrong type of server
	private boolean doNotSendAddRemoveEntityMsgs = false;

	protected SimpleGameData gameData = new SimpleGameData();

	public AbstractGameServer(String _gameID, GameOptions _gameOptions, int _tickrateMillis, int sendUpdateIntervalMillis, int _clientRenderDelayMillis, int _timeoutMillis) { 
		//float gravity, float aerodynamicness) {
		super();

		gameCode = _gameID;
		gameOptions = _gameOptions;
		tickrateMillis = _tickrateMillis;
		clientRenderDelayMillis = _clientRenderDelayMillis;
		timeoutMillis = _timeoutMillis;

		sendEntityUpdatesInterval = new RealtimeInterval(sendUpdateIntervalMillis);

		physicsController = new SimplePhysicsController<PhysicalEntity>(this, 15); // todo - get 15,1 params from parent

		loopTimer = new FixedLoopTime(tickrateMillis);

		setShowSettings(false); // Don't show settings dialog
		setPauseOnLostFocus(false);
	}


	@Override
	public void simpleInitApp() {
		try {
			gameNetworkServer = new KryonetGameServer(gameOptions.ourExternalPort, gameOptions.ourExternalPort, this, timeoutMillis, getListofMessageClasses());
			Globals.p("Listening on port " + gameOptions.ourExternalPort);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		assetManager.registerLocator("assets/", FileLocator.class); // default
		assetManager.registerLocator("assets/", ClasspathLocator.class);

		this.gameStatusSystem = new ServerGameStatusSystem(this);
		this.pingSystem = new ServerPingSystem(this);

		// Start console
		new TextConsole(this);

		startNewGame(); // Even though there are no players connected, this created the gameData to avoid NPEs.

		loopTimer.start();
	}


	/**
	 *  
	 * @return a list of classes that must be registered in order to be sent from client to server or vice-versa.
	 */
	protected abstract Class[] getListofMessageClasses();


	@Override
	public void simpleUpdate(float tpf_secs) {
		if (Globals.STRICT) {
			if (this.physicsController.getEntities().size() > this.entities.size()) {
				Globals.pe("Warning: more simple rigid bodies than entities!");
			}
		}

		/*if (gameOptions.lobbyip != null) {
		if (updateLobbyInterval.hitInterval()) {
			if (clientToLobbyServer == null) {
				connectToLobby();
			}
			if (clientToLobbyServer != null) {
				boolean spaces = this.doWeHaveSpaces();
				this.clientToLobbyServer.sendMessageToServer(new UpdateLobbyMessage(gameOptions.displayName, gameOptions.ourExternalIP, gameOptions.ourExternalPort, this.clients.size(), spaces));
			}
		}
	}*/

		// Add/remove queued clients
		synchronized (clientsToAdd) {
			while (this.clientsToAdd.size() > 0) {
				ClientData client = this.clientsToAdd.remove();
				this.clients.put(client.id, client);
				this.gameNetworkServer.sendMessageToClient(client, new WelcomeClientMessage());
				//Globals.p("Actually added client " + client.id);
			}
		}

		synchronized (clientsToRemove) {
			while (this.clientsToRemove.size() > 0) {
				int id = this.clientsToRemove.remove();
				ClientData client = this.clients.remove(id);
				if (client != null) {
					this.playerLeft(client);
				}
				Globals.p("Actually removed client " + id);
			}
		}

		if (gameNetworkServer.getNumClients() > 0) {
			// Process all messages
			synchronized (unprocessedMessages) {
				while (!this.unprocessedMessages.isEmpty()) {
					MyAbstractMessage message = this.unprocessedMessages.remove(0);
					ClientData client = message.client;

					if (message instanceof NewPlayerRequestMessage) {
						this.playerConnected(client, message);

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
					} else {
						throw new RuntimeException("Unknown message type: " + message);
					}
				}
			}

			// Add and remove entities - do this as close to the list iteration as possible!
			this.actuallyRemoveEntities();

			synchronized (this.clients) {
				// If any avatars are shooting a gun the requires "rewinding time", rewind all avatars and calc the hits all together to save time
				boolean areAnyPlayersShooting = false;
				for (ClientData c : this.clients.values()) {
					AbstractServerAvatar avatar = c.avatar;
					if (avatar != null && avatar.getAnyAbilitiesShootingInPast() != null) { //.isShooting() && avatar.abilityGun instanceof ICalcHitInPast) {
						areAnyPlayersShooting = true;
						break;
					}
				}
				if (areAnyPlayersShooting) {
					long timeTo = System.currentTimeMillis() - clientRenderDelayMillis; // Should this be by their ping time?
					this.rewindEntities(timeTo);
					this.rootNode.updateGeometricState();
					for (ClientData c : this.clients.values()) {
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

			boolean sendUpdates = sendEntityUpdatesInterval.hitInterval();
			EntityUpdateMessage eum = null;
			if (sendUpdates) {
				eum = new EntityUpdateMessage();
			}

			if (Globals.STRICT) {
				for(IEntity e : this.entities.values()) {
					if (e.requiresProcessing()) {
						if (!this.entitiesForProcessing.contains(e)) {
							Globals.p("Warning: Processed entity " + e + " not in process list!");
						}
					}
				}
			}


			int numSent = 0;
			// Loop through the entities
			//for (IEntity e : entitiesForProcessing.values()) { // this.entities
			for (int i=0 ; i<this.entitiesForProcessing.size() ; i++) {
				IEntity e = this.entitiesForProcessing.get(i);
				if (e.hasNotBeenRemoved()) {
					if (e instanceof IPlayerControlled) {
						IPlayerControlled p = (IPlayerControlled)e;
						p.resetPlayerInput();
					}

					if (e instanceof IProcessByServer) {
						IProcessByServer p = (IProcessByServer)e;
						p.processByServer(this, tpf_secs);
					}

					if (e instanceof PhysicalEntity) {
						PhysicalEntity physicalEntity = (PhysicalEntity)e;
						//strDebug.append(e.getID() + ": " + e.getName() + " Pos: " + physicalEntity.getWorldTranslation() + "\n");
						if (sendUpdates) {
							if (physicalEntity.sendUpdates()) { // Don't send if not moved (unless Avatar)
								eum.addEntityData(physicalEntity, false, physicalEntity.createEntityUpdateDataRecord());
								numSent++;
								physicalEntity.sendUpdate = false;
								if (eum.isFull()) {
									gameNetworkServer.sendMessageToAll(eum);	
									eum = new EntityUpdateMessage();
								}
							}
						}
					}
				}
			}

			if (sendUpdates) {
				gameNetworkServer.sendMessageToAll(eum);	
			}
			if (Globals.SHOW_NUM_ENT_UPDATES_SENT) {
				if (sendUpdates) {
					Globals.p("Num entity updates sent: " + numSent);
				}
			}
		}

		if (checkGameStatusInterval.hitInterval()) {
			gameStatusSystem.checkGameStatus(false);
		}

		this.pingSystem.process();

		loopTimer.waitForFinish(); // Keep clients and server running at same speed
		loopTimer.start();
	}


	protected synchronized void playerConnected(ClientData client, MyAbstractMessage message) {
		NewPlayerRequestMessage newPlayerMessage = (NewPlayerRequestMessage) message;
		if (!newPlayerMessage.gameCode.equalsIgnoreCase(gameCode)) {
			this.gameNetworkServer.sendMessageToClient(client, new JoinGameFailedMessage("Invalid Game code"));
			return;
		}
		if (!this.doWeHaveSpaces()) {
			this.gameNetworkServer.sendMessageToClient(client, new JoinGameFailedMessage("No spaces available"));
			return;
		}

		client.clientStatus = ClientData.ClientStatus.Accepted;
		client.side = getSide(client);
		client.playerData = new SimplePlayerData(client.id, newPlayerMessage.playerName, client.side);
		gameNetworkServer.sendMessageToClient(client, new GameSuccessfullyJoinedMessage(client.getPlayerID(), client.side)); // Must be before we send the avatar so they know it's their avatar
		sendGameStatusMessage(); // So they have a game ID, required when receiving ents
		client.avatar = createPlayersAvatar(client);
		sendGameStatusMessage();
		sendAllEntitiesToClient(client);
		this.gameNetworkServer.sendMessageToClient(client, new SetAvatarMessage(client.getPlayerID(), client.avatar.getID()));
		this.pingSystem.sendPingToClient(client);
		this.gameNetworkServer.sendMessageToAllExcept(client, new GenericStringMessage("Player joined!", true));
		playerJoinedGame(client);
		gameStatusSystem.checkGameStatus(true);

	}


	public void sendGameStatusMessage() {
		ArrayList<SimplePlayerData> players = new ArrayList<SimplePlayerData>();
		for(ClientData client : this.clients.values()) {
			if (client.clientStatus == ClientStatus.Accepted) {
				players.add(client.playerData);
			}
		}
		//Globals.p("Sending SimpleGameDataMessage with Game ID of " + gameData.gameID);
		this.gameNetworkServer.sendMessageToAll(new SimpleGameDataMessage(this.gameData, players));


	}


	public abstract boolean doWeHaveSpaces();


	protected void removeOldGame() {
		if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
			Globals.p("Removing all entities");
		}

		// remove All Entities
		/*synchronized (this.clients) {
			for (ClientData c : this.clients.values()) {
				AbstractServerAvatar avatar = c.avatar;
				if (avatar != null) {
					avatar.remove();
					c.avatar = null;
				}
			}
		}*/

		for (IEntity e : this.entities.values()) {
			e.remove();
		}
		this.actuallyRemoveEntities();

	}
	
	
	private void actuallyRemoveEntities() {
		for(Integer i : this.entitiesToRemove) {
			this.actuallyRemoveEntity(i);
		}
		this.entitiesToRemove.clear();

	}


	protected void startNewGame() {
		if (this.entities.size() > 0) {
			throw new RuntimeException("Outstanding entities");
		}
		if (this.entitiesToRemove.size() > 0) {
			throw new RuntimeException("Entities waiting to be removed");
		}

		if (this.getGameNode().getChildren().size() > 0) {
			Globals.p("Warning: There are still " + this.getGameNode().getChildren().size() + " children in the game node!  Forcing removal...");
			this.getGameNode().detachAllChildren();
		}
		if (this.getPhysicsController().getEntities().size() > 0) {
			Globals.p("Warning: There are still " + this.getPhysicsController().getEntities().size() + " children in the physics world!  Forcing removal...");
			this.getPhysicsController().removeAllEntities();
		}

		try {
			doNotSendAddRemoveEntityMsgs = true; // Prevent sending new ent messages for all the entities
			this.createGame();
		} finally {
			doNotSendAddRemoveEntityMsgs = false;
		}

		// Create avatars and send new entities to players
		synchronized (this.clients) {
			for (ClientData client : this.clients.values()) {
				if (client.clientStatus == ClientData.ClientStatus.Accepted) {
					int side = getSide(client); // New sides
					client.playerData.side = side;
					client.avatar = createPlayersAvatar(client);
					sendAllEntitiesToClient(client);
					this.gameNetworkServer.sendMessageToClient(client, new SetAvatarMessage(client.getPlayerID(), client.avatar.getID()));
				}
			}
		}

	}


	/**
	 * Determine the side for a player
	 * @param client
	 * @return The side
	 */
	public abstract int getSide(ClientData client);

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
		synchronized (clients) {
			client = clients.get(clientid);
		}

		if (client == null) {
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


	//public abstract float getAvatarStartHealth(AbstractAvatar avatar);
/*
	public float getAvatarMoveSpeed(AbstractAvatar avatar) { // todo - move to constructor
		return 3f; // Override if required
	}


	public float getAvatarJumpForce(AbstractAvatar avatar) { // todo - move to constructor
		return 2f; // Override if required
	}
*/
	public abstract void moveAvatarToStartPosition(AbstractAvatar avatar);

	protected abstract AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid);

	protected void sendAllEntitiesToClient(ClientData client) {
		NewEntityMessage nem = new NewEntityMessage(this.getGameID());
		synchronized (entities) {
			for (IEntity e : entities.values()) {
				if (e.getGameID() == this.getGameID()) { // Since we might still have old entities that have not actually been removed!
					nem.data.add(new NewEntityData(e));
					if (nem.isFull()) {
						this.gameNetworkServer.sendMessageToClient(client, nem);
						Functions.sleep(50); // Try prevent buffer overflow
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
		synchronized (clientsToAdd) {
			clientsToAdd.add(client);
		}
	}


	@Override
	public void connectionRemoved(int id) {
		Globals.p("connectionRemoved()");
		this.clientsToRemove.add(id);
	}


	protected void playerLeft(ClientData client) {
		Globals.p("Removing player " + client.getPlayerID());
		// Remove avatar
		if (client.avatar != null) {
			client.avatar.remove();
		}

		this.gameNetworkServer.sendMessageToAllExcept(client, new GenericStringMessage("Player left!", true));
		this.sendGameStatusMessage();
		gameStatusSystem.checkGameStatus(true);
	}


	@Override
	public void addEntity(IEntity e) {
		if (e == null) {
			throw new RuntimeException("Trying to add null entity");
		}
		//this.entitiesToAdd.add(e);
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
			Node parent = pe.getOwnerNode();
			if (parent != null) {
				parent.attachChild(pe.getMainNode());
			} else {
				this.getGameNode().attachChild(pe.getMainNode());
			}
			if (pe.simpleRigidBody != null) {
				this.getPhysicsController().addSimpleRigidBody(pe.simpleRigidBody);
			}
		}

		if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
			Globals.p("Created and added " + e);
		}

		// Tell clients
		if (!doNotSendAddRemoveEntityMsgs) {
		NewEntityMessage nem = new NewEntityMessage(this.getGameID());
		nem.data.add(new NewEntityData(e));
		synchronized (clients) {
			for (ClientData client : this.clients.values()) {
				if (client.clientStatus == ClientStatus.Accepted) {
					gameNetworkServer.sendMessageToClient(client, nem);
				}
			}
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
			IEntity e = this.entities.get(id); // this.entitiesToAdd
			if (e != null) {
				if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
					Globals.p("Actually removing entity " + e + (this.doNotSendAddRemoveEntityMsgs ? "":" ..and sending message to clients"));
				}
				this.entities.remove(id);
				if (e.requiresProcessing()) {
					this.entitiesForProcessing.remove(e);
				}
			}
			/*if (e instanceof IClientControlled) {  No! Still tell client when to remove it
				IClientControlled cc = (IClientControlled)e;
				if (cc.isClientControlled()) {
					if (Globals.DEBUG_NO_BULLET) {
						Globals.p("NOT sending bullet remove");
					}
					return; // todo - remove?
				}
			}*/
			if (!this.doNotSendAddRemoveEntityMsgs) {
				this.gameNetworkServer.sendMessageToAll(new RemoveEntityMessage(id));
			}
		}

	}


	@Override
	public void messageReceived(MyAbstractMessage message) {
		if (Globals.DEBUG_MSGS) {
			Globals.p("Rcvd " + message.getClass().getSimpleName());
		}

		// Add it to list for processing in main thread
		synchronized (this.unprocessedMessages) {
			this.unprocessedMessages.add(message);
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


	public void collisionOccurred(PhysicalEntity pea, PhysicalEntity peb) {
		if (pea instanceof INotifiedOfCollision) {
			INotifiedOfCollision ic = (INotifiedOfCollision)pea;
			ic.collided(peb);
		}
		if (peb instanceof INotifiedOfCollision) {
			INotifiedOfCollision ic = (INotifiedOfCollision)peb;
			ic.collided(pea);
		}

		collisionLogic.collision(pea, peb);
	}


	@Override
	public SimplePhysicsController<PhysicalEntity> getPhysicsController() {
		return physicsController;
	}


	private void rewindEntities(long toTime) {
		synchronized (this.clients) {
			for (IEntity e : this.entitiesForProcessing) {
				if (e instanceof IRewindable) {
					IRewindable r = (IRewindable)e;
					r.rewindPositionTo(toTime);
				}
			}
		}
	}


	private void restoreEntityPositions() {
		synchronized (this.clients) {
			for (IEntity e : entitiesForProcessing) {
				if (e instanceof IRewindable) {
					IRewindable r = (IRewindable)e;
					r.restorePosition();
				}
			}
		}
	}


	public PhysicalEntity getTarget(PhysicalEntity shooter, int ourSide) {
		for (IEntity e : entitiesForProcessing) {
			if (e != shooter) {
				if (e instanceof ITargetable) {
					ITargetable t = (ITargetable)e;
					if (t.isAlive() && t.isValidTargetForSide(ourSide)) {
						PhysicalEntity pe = (PhysicalEntity)e;
						if (shooter.canSee(pe, 100)) {
							return pe;
						}
					}
				}
			}
		}
		return null; 
	}


	@Override
	public int getGameID() {
		return this.gameData.gameID;
	}


	public void gameStatusChanged(int newStatus) {
		if (newStatus == SimpleGameData.ST_DEPLOYING) {
			this.gameNetworkServer.sendMessageToAll(new GeneralCommandMessage(GeneralCommandMessage.Command.GameRestarting));

			this.gameNetworkServer.sendMessageToAll(new GeneralCommandMessage(GeneralCommandMessage.Command.RemoveAllEntities)); // Before we increment the game id!
			try {
				doNotSendAddRemoveEntityMsgs = true; // Prevent sending "remove entities" messages for all the entities
				removeOldGame();
			} finally {
				doNotSendAddRemoveEntityMsgs = false;
			}

			this.gameData.gameID = nextGameID.getAndAdd(1);
			sendGameStatusMessage(); // To send the new game ID
			Globals.p("------------------------------");
			Globals.p("Starting new game " + gameData.gameID);
			startNewGame();
			this.gameNetworkServer.sendMessageToAll(new GeneralCommandMessage(GeneralCommandMessage.Command.GameRestarted));
			
		} else if (newStatus == SimpleGameData.ST_STARTED) {
			synchronized (entities) {
				for (IEntity e : entities.values()) {
					if (e instanceof IGetReadyForGame) {
						IGetReadyForGame grfg = (IGetReadyForGame)e;
						grfg.getReadyForGame();
					}
				}
			}
			
		} else if (newStatus == SimpleGameData.ST_FINISHED) {
			int winningSide = this.getWinningSide();
			this.gameNetworkServer.sendMessageToAll(new GameOverMessage(winningSide));
		}
	}


	protected abstract int getWinningSide();


	public abstract int getMinPlayersRequiredForGame();


	@Override
	public synchronized int getNumEntities() {
		return this.entities.size();
	}


	@Override
	public void addClientOnlyEntity(IEntity e) {
		throw new RuntimeException("Trying to add client-only entity to server");
	}


	public void handleCommand(String cmd) {
		if (cmd.equals("warp")) {
			for(ClientData client : this.clients.values()) {
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
	public void connected() {
		// Connected to lobby server

	}


	@Override
	public void disconnected() {
		Globals.p("Disconnected from lobby server");

	}


	@Override
	public Node getGameNode() {
		return rootNode;
	}


	@Override
	public void processConsoleInput(String s) {
		//Globals.p("Received input: " + s);
		if (s.equalsIgnoreCase("help") || s.equalsIgnoreCase("?")) {
			Globals.p("mb, stats, entities");
		} else if (s.equalsIgnoreCase("mb")) {
			sendDebuggingBoxes();
		} else if (s.equalsIgnoreCase("stats")) {
			showStats();
		} else if (s.equalsIgnoreCase("entities") || s.equalsIgnoreCase("e")) {
			listEntities();
		} else {
			Globals.p("Unknown command: " + s);
		}

	}


	private void sendDebuggingBoxes() {
		synchronized (entities) {
			// Loop through the entities
			for (IEntity e : entities.values()) {
				if (e instanceof PhysicalEntity) {
					PhysicalEntity pe  = (PhysicalEntity)e;
					this.gameNetworkServer.sendMessageToAll(new ModelBoundsMessage(pe));
				}
			}
		}
		Globals.p("Sent model bounds to all clients");
	}


	private void listEntities() {
		synchronized (entities) {
			// Loop through the entities
			for (IEntity e : entities.values()) {
				Globals.p("Entity " + e.getID() + ": " + e.getName() + " (" + e + ")");
			}
			Globals.p("Total:" + getNumEntities());
		}
	}


	private void showStats() {
		Globals.p("Game ID: " + this.getGameID());
		Globals.p("Num Entities: " + this.entities.size());
		Globals.p("Num Entities for processing: " + this.entitiesForProcessing.size());
		Globals.p("Num Clients: " + this.clients.size());
	}


	public void playerKilled(AbstractServerAvatar avatar) {
		// Override if req
	}


	/*
	private void connectToLobby() {
		try {
			clientToLobbyServer = new KryonetLobbyClient(gameOptions.lobbyip, gameOptions.lobbyport, gameOptions.lobbyport, this, timeoutMillis);
			Globals.p("Connected to lobby server");
		} catch (IOException e) {
			Globals.p("Unable to connect to lobby server");
		}	
	}
	 */

	public SimpleGameData getGameData() {
		return this.gameData;
	}


	@Override
	public boolean canCollide(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		PhysicalEntity pa = a.userObject; //pa.getMainNode().getWorldBound();
		PhysicalEntity pb = b.userObject; //pb.getMainNode().getWorldBound();

		return canCollide(pa, pb);
	}


}
