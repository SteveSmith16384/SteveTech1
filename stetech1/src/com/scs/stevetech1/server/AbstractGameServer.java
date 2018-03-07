package com.scs.stevetech1.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.JmeContext;
import com.scs.simplephysics.ICollisionListener;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.ICalcHitInPast;
import com.scs.stevetech1.components.IClientControlled;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IGetReadyForGame;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.components.IPlayerControlled;
import com.scs.stevetech1.components.IProcessByServer;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.data.SimplePlayerData;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.lobby.KryonetLobbyClient;
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
import com.scs.stevetech1.netmessages.PlayerInputMessage;
import com.scs.stevetech1.netmessages.PlayerLeftMessage;
import com.scs.stevetech1.netmessages.RemoveEntityMessage;
import com.scs.stevetech1.netmessages.SimpleGameDataMessage;
import com.scs.stevetech1.netmessages.WelcomeClientMessage;
import com.scs.stevetech1.netmessages.lobby.UpdateLobbyMessage;
import com.scs.stevetech1.networking.IGameMessageServer;
import com.scs.stevetech1.networking.IMessageClientListener;
import com.scs.stevetech1.networking.IMessageServerListener;
import com.scs.stevetech1.networking.KryonetGameServer;
import com.scs.stevetech1.server.ClientData.ClientStatus;
import com.scs.stevetech1.shared.AbstractGameController;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.systems.server.ServerGameStatusSystem;

import ssmith.lang.NumberFunctions;
import ssmith.util.ConsoleInputListener;
import ssmith.util.RealtimeInterval;
import ssmith.util.TextConsole;

public abstract class AbstractGameServer extends AbstractGameController implements 
IEntityController, 
IMessageServerListener, // To listen for connecting game clients 
IMessageClientListener, // For sending messages to the lobby server
ICollisionListener<PhysicalEntity>,
ConsoleInputListener {

	public IGameMessageServer gameNetworkServer;
	private KryonetLobbyClient clientToLobbyServer;
	public HashMap<Integer, ClientData> clients = new HashMap<>(10); // PlayerID::ClientData
	private LinkedList<ClientData> clientsToAdd = new LinkedList<>();
	private LinkedList<Integer> clientsToRemove = new LinkedList<>();

	private RealtimeInterval updateLobbyInterval = new RealtimeInterval(30 * 1000);
	private RealtimeInterval checkGameStatusInterval = new RealtimeInterval(5000);
	private RealtimeInterval sendEntityUpdatesInterval;// = new RealtimeInterval(Globals.SERVER_SEND_UPDATE_INTERVAL_MS);

	private List<MyAbstractMessage> unprocessedMessages = new LinkedList<>();
	public SimpleGameData gameData;
	public ServerSideCollisionLogic collisionLogic = new ServerSideCollisionLogic();
	public GameOptions gameOptions;
	private int randomPingCode = NumberFunctions.rnd(0,  999999);

	// Systems
	private ServerGameStatusSystem gameStatusSystem;

	public AbstractGameServer(GameOptions _gameOptions, int tickrateMillis, int sendUpdateIntervalMillis, int clientRenderDelayMillis, int timeoutMillis, float gravity, float aerodynamicness) throws IOException {
		super(tickrateMillis, clientRenderDelayMillis, timeoutMillis);

		gameOptions = _gameOptions;
		sendEntityUpdatesInterval = new RealtimeInterval(sendUpdateIntervalMillis);

		gameData = new SimpleGameData();
		gameNetworkServer = new KryonetGameServer(gameOptions.ourExternalPort, gameOptions.ourExternalPort, this, timeoutMillis);

		Globals.p("Listening on port " + gameOptions.ourExternalPort);

		physicsController = new SimplePhysicsController<PhysicalEntity>(this, gravity, aerodynamicness);

		this.gameStatusSystem = new ServerGameStatusSystem(this);

		setShowSettings(false); // Don't show settings dialog
		setPauseOnLostFocus(false);
		start(JmeContext.Type.Headless);
	}


	@Override
	public void simpleInitApp() {
		// Start console
		new TextConsole(this);

		assetManager.registerLocator("assets/", FileLocator.class); // default
		assetManager.registerLocator("assets/", ClasspathLocator.class);

		createGame();

		loopTimer.start();
	}


	protected abstract void createGame();


	private void connectToLobby() {
		try {
			clientToLobbyServer = new KryonetLobbyClient(gameOptions.lobbyip, gameOptions.lobbyport, gameOptions.lobbyport, this, timeoutMillis);
			Globals.p("Connected to lobby server");
		} catch (IOException e) {
			Globals.p("Unable to connect to lobby server");
		}	
	}


	@Override
	public void simpleUpdate(float tpf_secs) {
		StringBuilder strDebug = new StringBuilder(); // todo - remove

		if (updateLobbyInterval.hitInterval()) {
			if (clientToLobbyServer == null) {
				connectToLobby();
			}
			if (clientToLobbyServer != null) {
				boolean spaces = this.doWeHaveSpaces();
				this.clientToLobbyServer.sendMessageToServer(new UpdateLobbyMessage(gameOptions.displayName, gameOptions.ourExternalIP, gameOptions.ourExternalPort, this.clients.size(), spaces));
			}
		}


		// Add/remove queued clients
		synchronized (clientsToAdd) {
			while (this.clientsToAdd.size() > 0) {
				ClientData client = this.clientsToAdd.remove();
				this.clients.put(client.id, client);
				this.gameNetworkServer.sendMessageToClient(client, new WelcomeClientMessage());
				Globals.p("Actually added client " + client.id);
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

		// Add and remove entities
		synchronized (entities) {
			for(IEntity e : this.entitiesToAdd) {
				this.actuallyAddEntity(e, true);
			}
			this.entitiesToAdd.clear();

			for(Integer i : this.entitiesToRemove) {
				this.actuallyRemoveEntity(i);
			}
			this.entitiesToRemove.clear();
		}

		if (gameNetworkServer.getNumClients() > 0) {
			// Process all messages
			synchronized (unprocessedMessages) {
				while (!this.unprocessedMessages.isEmpty()) {
					MyAbstractMessage message = this.unprocessedMessages.remove(0);
					ClientData client = message.client;

					if (message instanceof NewPlayerRequestMessage) {
						this.playerConnected(client, message);

						/*} else if (message instanceof UnknownEntityMessage) {
						UnknownEntityMessage uem = (UnknownEntityMessage) message;
						IEntity e = null;
						synchronized (entities) {
							e = this.entities.get(uem.entityID);
						}
						this.sendNewEntity(client, e);*/

					} else if (message instanceof PlayerLeftMessage) {
						this.connectionRemoved(client.getPlayerID());

					} else if (message instanceof PlayerInputMessage) { // Process these here so the inputs don't change values mid-thread
						PlayerInputMessage pim = (PlayerInputMessage)message;
						if (pim.timestamp > client.latestInputTimestamp) {
							client.remoteInput.decodeMessage(pim);
							client.latestInputTimestamp = pim.timestamp;
						}

					} else {
						throw new RuntimeException("Unknown message type: " + message);
					}
				}
			}

			if (sendPingInterval.hitInterval()) {
				randomPingCode = NumberFunctions.rnd(0,  999999);
				this.gameNetworkServer.sendMessageToAll(new PingMessage(true, randomPingCode));
			}

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
							Vector3f from = avatar.getBulletStartPos();
							if (Globals.DEBUG_SHOOTING_POS) {
								Globals.p("Server shooting from " + from);
							}
							Ray ray = new Ray(from, avatar.getShootDir());
							RayCollisionData rcd = avatar.checkForCollisions(ray, chip.getRange());
							if (rcd != null) {
								rcd.timestamp = timeTo; // For debugging
							}
							chip.setTarget(rcd); // Damage etc.. is calculated later
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

			synchronized (entities) {
				// Loop through the entities
				for (IEntity e : entities.values()) {
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
						if (Globals.STRICT) {
							if (physicalEntity.simpleRigidBody != null) {
								if (physicalEntity.simpleRigidBody.canMove() != physicalEntity.moves) {
									// Todo - fix this, handloe kinematic objects
									//Globals.pe("Warning!  Entity " + physicalEntity.name + ": Discrepancy between canMove() in rigid body and entity");
								}
							}
						}
						//strDebug.append(e.getID() + ": " + e.getName() + " Pos: " + physicalEntity.getWorldTranslation() + "\n");
						if (sendUpdates) {
							if (physicalEntity.sendUpdates()) { // Don't send if not moved (unless Avatar)
								eum.addEntityData(physicalEntity, false);
								if (eum.isFull()) {
									gameNetworkServer.sendMessageToAll(eum);	
									eum = new EntityUpdateMessage();
								}
							}
						}
					} else {
						strDebug.append(e.getID() + ": " + e.getName() + "\n");
					}
				}
			}
			if (sendUpdates) {
				gameNetworkServer.sendMessageToAll(eum);	
			}
			if (checkGameStatusInterval.hitInterval()) {
				//this.checkGameStatus(false);
				gameStatusSystem.checkGameStatus(false);
			}
		}

		//this.logWindow.setText(strDebug.toString());

		loopTimer.waitForFinish(); // Keep clients and server running at same speed
		loopTimer.start();
	}


	private boolean doWeHaveSpaces() {
		if (this.gameOptions.maxSides <= 0 || this.gameOptions.maxPlayersPerSide <= 0) {
			return true;
		}
		int currentPlayers = 0;
		for(ClientData c : this.clients.values()) {
			if (c.clientStatus == ClientData.ClientStatus.Accepted) {  // only count players actually Accepted!
				currentPlayers++;
			}
		}
		int maxPlayers = this.gameOptions.maxSides * this.gameOptions.maxPlayersPerSide;
		return currentPlayers < maxPlayers;
	}


	private synchronized void playerConnected(ClientData client, MyAbstractMessage message) {
		if (!this.doWeHaveSpaces()) {
			this.gameNetworkServer.sendMessageToClient(client, new JoinGameFailedMessage("No spaces"));
		}

		NewPlayerRequestMessage newPlayerMessage = (NewPlayerRequestMessage) message;
		client.side = getSide(client);
		client.playerData = new SimplePlayerData(client.id, newPlayerMessage.name, client.side);
		gameNetworkServer.sendMessageToClient(client, new GameSuccessfullyJoinedMessage(client.getPlayerID(), client.side));//, client.avatar.id)); // Must be before we send the avatar so they know it's their avatar
		client.avatar = createPlayersAvatar(client);
		sendAllEntitiesToClient(client);
		client.clientStatus = ClientData.ClientStatus.Accepted;

		this.sendGameStatusMessage();

		this.gameNetworkServer.sendMessageToClient(client, new PingMessage(true, this.randomPingCode));		
		this.gameNetworkServer.sendMessageToAllExcept(client, new GenericStringMessage("Player joined!", true));

		gameStatusSystem.checkGameStatus(true);

	}


	private int getSide(ClientData client) {
		if (this.gameOptions.areAllPlayersOnDifferentSides()) {
			return client.id;
		} else {
			// todo - Check maxPlayersPerside, maxSides
			HashMap<Integer, Integer> map = getPlayersPerSide();
			// Get lowest amount
			int lowest = 999;
			int highest = -1;
			for (int i : map.values()) {
				if (i < lowest) {
					lowest = i;
				}
				if (i > highest) {
					highest = i;
				}
			}
			// Get the side
			Iterator<Integer> it = map.keySet().iterator();
			while (it.hasNext()) {
				int i = it.next();
				int val = map.get(i);
				if (val <= lowest) {
					return i;
				}
			}
			throw new RuntimeException("Should not get here");
		}
	}


	private HashMap<Integer, Integer> getPlayersPerSide() {
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (ClientData client : this.clients.values()) {
			if (client.avatar != null) {
				if (!map.containsKey(client.side)) {
					map.put(client.side, 0);
				}
				int val = map.get(client.side);
				val++;
				map.put(client.side, val);
			}
		}
		return map;
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

		if (message instanceof PingMessage) {
			PingMessage pingMessage = (PingMessage) message;
			if (pingMessage.s2c) {
				try {
					// Check code
					if (pingMessage.randomCode == this.randomPingCode) {
						try {
							long rttDuration = System.currentTimeMillis() - pingMessage.originalSentTime;
							if (client.playerData != null) {
								client.playerData.pingRTT = client.pingCalc.add(rttDuration);
								client.serverToClientDiffTime = pingMessage.responseSentTime - pingMessage.originalSentTime - (client.playerData.pingRTT/2); // If running on the same server, this should be 0! (or close enough)
								//Settings.p("Client rtt = " + client.pingRTT);
								//Settings.p("serverToClientDiffTime = " + client.serverToClientDiffTime);
								if ((client.playerData.pingRTT/2) + sendEntityUpdatesInterval.getInterval() > clientRenderDelayMillis) {
									Globals.p("Warning: client ping is longer than client render delay!");
								}
							}
						} catch (NullPointerException ex) {
							Globals.HandleError(ex);
						}
					} else {
						Globals.pe("Unexpected ping response code!");
					}
				} catch (NullPointerException npe) {
					Globals.HandleError(npe);
				}
			} else {
				// Send it back to the client
				pingMessage.responseSentTime = System.currentTimeMillis();
				this.gameNetworkServer.sendMessageToClient(client, pingMessage);
			}

		} else {
			msg.client = client;
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
		this.actuallyAddEntity(avatar, true);
		return avatar;
	}


	public abstract float getAvatarStartHealth(AbstractAvatar avatar);

	public abstract float getAvatarMoveSpeed(AbstractAvatar avatar);

	public abstract float getAvatarJumpForce(AbstractAvatar avatar);

	public abstract void moveAvatarToStartPosition(AbstractAvatar avatar);

	protected abstract AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid);

	private void sendAllEntitiesToClient(ClientData client) {
		synchronized (entities) {
			for (IEntity e : entities.values()) {
				if (Globals.DEBUG_TOO_MANY_AVATARS) {
					if (e instanceof AbstractAvatar) {
						Globals.p("Sending avatar msg");
					}
				}

				NewEntityMessage nem = new NewEntityMessage(e);
				this.gameNetworkServer.sendMessageToClient(client, nem);
			}
			GeneralCommandMessage aes = new GeneralCommandMessage(GeneralCommandMessage.Command.AllEntitiesSent);
			this.gameNetworkServer.sendMessageToClient(client, aes);
		}
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
		/*synchronized (clients) {
			ClientData client = clients.get(id);
			if (client != null) { // For some reason, connectionRemoved() gets called multiple times
				this.playerLeft(client);
			}
		}*/
		this.clientsToRemove.add(id);
	}


	protected void playerLeft(ClientData client) {
		Globals.p("Removing player " + client.getPlayerID());
		/*synchronized (clients) { No longer in the list
			this.clients.remove(client.getPlayerID());
		}*/
		// Remove avatar
		if (client.avatar != null) {
			client.avatar.remove();
		}

		this.gameNetworkServer.sendMessageToAllExcept(client, new GenericStringMessage("Player joined!", true));
		this.sendGameStatusMessage();
		gameStatusSystem.checkGameStatus(true);
	}


	@Override
	public void addEntity(IEntity e) {
		this.entitiesToAdd.add(e);
	}


	public void actuallyAddEntity(IEntity e) {
		this.actuallyAddEntity(e, false);
	}


	public void actuallyAddEntity(IEntity e, boolean sendToClients) {
		synchronized (entities) {
			//Settings.p("Trying to add " + e + " (id " + e.getID() + ")");
			if (this.entities.containsKey(e.getID())) {
				throw new RuntimeException("Entity id " + e.getID() + " already exists: " + e);
			}
			this.entities.put(e.getID(), e);
		}
		if (e instanceof PhysicalEntity) {
			PhysicalEntity pe = (PhysicalEntity)e;
			if (pe.getMainNode().getParent() != null) {
				throw new RuntimeException("Entity already has a node");
			}
			this.getGameNode().attachChild(pe.getMainNode()); //pe.getMainNode().getWorldTranslation();
		}

		if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
			Globals.p("Created and added " + e);
		}

		// Tell clients
		//if (sendToClients) {
		if (Globals.DEBUG_TOO_MANY_AVATARS) {
			if (e instanceof AbstractAvatar) {
				Globals.p("Sending avatar msg");
			}
		}
		NewEntityMessage nem = new NewEntityMessage(e);
		synchronized (clients) {
			for (ClientData client : this.clients.values()) {
				if (client.clientStatus == ClientStatus.Accepted) {
					gameNetworkServer.sendMessageToClient(client, nem);	
				}
			}
		}
		//}
	}


	@Override
	public void removeEntity(int id) {
		this.entitiesToRemove.add(id);
	}


	private void actuallyRemoveEntity(int id) {
		synchronized (entities) {
			IEntity e = this.entities.get(id); // this.entitiesToAdd
			if (e != null) {
				if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
					Globals.p("Actually removing entity " + e.getName() + " / ID:" + id);
				}
				this.entities.remove(id);
				//this.console.appendText("Removed " + e);
			} else {
				//Globals.pe("Warning - entity " + id + " doesn't exist for removal");  Probably an entity that is owned by another removed entity, e.g. SnowballLauncher
			}

			if (e instanceof IClientControlled) {
				IClientControlled cc = (IClientControlled)e;
				if (cc.isClientControlled()) {
					return;
				}
			}
			this.gameNetworkServer.sendMessageToAll(new RemoveEntityMessage(id));
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


	private void rewindEntities(long toTime) {
		synchronized (this.clients) {
			for (IEntity e : entities.values()) {
				if (e instanceof IRewindable) {
					IRewindable r = (IRewindable)e;
					r.rewindPositionTo(toTime);
				}
			}
		}
	}


	private void restoreEntityPositions() {
		synchronized (this.clients) {
			for (IEntity e : entities.values()) {
				if (e instanceof IRewindable) {
					IRewindable r = (IRewindable)e;
					r.restorePosition();
				}
			}
		}
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


	private void removeOldGame() {
		if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
			Globals.p("Removing all entities");
		}

		// remove All Entities
		synchronized (this.clients) {
			for (ClientData c : this.clients.values()) {
				AbstractServerAvatar avatar = c.avatar;
				if (avatar != null) {
					avatar.remove();
					c.avatar = null;
				}
			}
		}

		for (IEntity e : this.entities.values()) {
			e.remove();
		}

		//this.entitiesToAdd.clear();  Not needed?
		//this.entitiesToRemove.clear(); Not needed?

	}


	private void startNewGame() {
		/*if (this.entities.size() > 0) {
			throw new RuntimeException("Outstanding entities");
		}
		if (this.entitiesToAdd.size() > 0) {
			throw new RuntimeException("Entities waiting to be added");
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
		 */
		this.createGame();

		// Create avatars and send new entities to players
		synchronized (this.clients) {
			for (ClientData client : this.clients.values()) {
				int side = getSide(client); // New sides
				client.playerData.side = side;
				client.avatar = createPlayersAvatar(client);
				sendAllEntitiesToClient(client);
			}
		}

	}


	public ArrayList<RayCollisionData> checkForEntityCollisions(Ray r) {
		CollisionResults res = new CollisionResults();
		ArrayList<RayCollisionData> myList = new ArrayList<RayCollisionData>(); 
		synchronized (entities) {
			// Loop through the entities
			for (IEntity e : entities.values()) {
				if (e instanceof PhysicalEntity) {
					PhysicalEntity ic = (PhysicalEntity)e;
					//r.collideWith(ic.getMainNode().getWorldBound(), res);
					res.clear();
					int c = ((PhysicalEntity) e).getMainNode().getWorldBound().collideWith(r, res);
					if (c > 0) {
						CollisionResult cr = res.getClosestCollision();
						RayCollisionData rcd = new RayCollisionData(ic, cr.getContactPoint(), cr.getDistance());
						myList.add(rcd);
					}
				}
			}
		}
		Collections.sort(myList);
		return myList;
	}


	@Override
	public void collisionOccurred(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b, Vector3f point) {
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

		collisionLogic.collision(pea, peb);
	}


	@Override
	public SimplePhysicsController<PhysicalEntity> getPhysicsController() {
		return physicsController;
	}


	public void sendGameStatusMessage() {
		ArrayList<SimplePlayerData> players = new ArrayList<SimplePlayerData>();
		for(ClientData client : this.clients.values()) {
			if (client.clientStatus == ClientStatus.Accepted) {
				players.add(client.playerData);
			}
		}
		this.gameNetworkServer.sendMessageToAll(new SimpleGameDataMessage(this.gameData, players));

	}


	@Override
	public void connected() {
		// Connected to lobby server

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
	public void disconnected() {
		Globals.p("Disconnected from lobby server");

	}


	/*	
	public boolean isAreaClear(Spatial s) {
		//SimplePhysicsController<String> spc = new SimplePhysicsController<String>(null);
		SimpleRigidBody<PhysicalEntity> srb = new SimpleRigidBody<PhysicalEntity>(s, this.physicsController, false, null);
		SimpleRigidBody<PhysicalEntity> collidedWith = srb.checkForCollisions();
		this.physicsController.removeSimpleRigidBody(srb);
		return collidedWith == null;

	}
	 */


	@Override
	public Node getGameNode() {
		return rootNode;
	}


	public void gameStatusChanged(int newStatus)  {
		/*if (newStatus == SimpleGameData.ST_CLEAR_OLD_GAME) {
		} else */
		if (newStatus == SimpleGameData.ST_DEPLOYING) {
			removeOldGame();
			startNewGame();
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


	@Override
	public void processConsoleInput(String s) {
		//Globals.p("Received input: " + s);
		if (s.equalsIgnoreCase("help") || s.equalsIgnoreCase("?")) {
			Globals.p("mb, stats, entities");
		} else if (s.equalsIgnoreCase("mb")) {
			sendDebuggingBoxes();
		} else if (s.equalsIgnoreCase("stats")) {
			showStats();
		} else if (s.equalsIgnoreCase("entities")) {
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
		}
	}


	private void showStats() {
		Globals.p("Num Entities: " + this.entities.size());
		Globals.p("Num Clients: " + this.clients.size());
	}

}

