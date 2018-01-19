package com.scs.stevetech1.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.scs.simplephysics.ICollisionListener;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.ICalcHitInPast;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.components.IPreprocess;
import com.scs.stevetech1.components.IProcessByServer;
import com.scs.stevetech1.components.IRequiresAmmoCache;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.data.SimplePlayerData;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.lobby.KryonetLobbyClient;
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
import com.scs.stevetech1.netmessages.RequestNewBulletMessage;
import com.scs.stevetech1.netmessages.UnknownEntityMessage;
import com.scs.stevetech1.netmessages.WelcomeClientMessage;
import com.scs.stevetech1.netmessages.lobby.UpdateLobbyMessage;
import com.scs.stevetech1.networking.IGameMessageServer;
import com.scs.stevetech1.networking.IMessageClientListener;
import com.scs.stevetech1.networking.IMessageServerListener;
import com.scs.stevetech1.networking.KryonetGameServer;
import com.scs.stevetech1.server.ClientData.ClientStatus;
import com.scs.stevetech1.shared.AbstractGameController;
import com.scs.stevetech1.shared.IEntityController;

import ssmith.swing.LogWindow;
import ssmith.util.RealtimeInterval;

public abstract class AbstractGameServer extends AbstractGameController implements 
			IEntityController, 
			IMessageServerListener, // To listen for connecting game clients 
			IMessageClientListener, // For sending messages to the lobby server
			ICollisionListener<PhysicalEntity> {

	private static final String PROPS_FILE = Globals.NAME.replaceAll(" ", "") + "_settings.txt";

	public IGameMessageServer networkServer;
	private KryonetLobbyClient clientToLobbyServer;
	private HashMap<Integer, ClientData> clients = new HashMap<>(10); // PlayerID::ClientData

	public static GameProperties properties;
	private RealtimeInterval updateLobbyInterval = new RealtimeInterval(5000);
	private RealtimeInterval checkStatusInterval = new RealtimeInterval(1000);
	private RealtimeInterval sendEntityUpdatesInterval = new RealtimeInterval(Globals.SERVER_SEND_UPDATE_INTERVAL_MS);
	private List<MyAbstractMessage> unprocessedMessages = new LinkedList<>();
	protected LogWindow logWindow;
	public IConsole console;
	protected SimpleGameData gameData;
	public CollisionLogic collisionLogic = new CollisionLogic();
	private GameOptions gameOptions;

	public AbstractGameServer(GameOptions _gameOptions) throws IOException {
		super();

		gameOptions = _gameOptions;

		properties = new GameProperties(PROPS_FILE);
		logWindow = new LogWindow("Server", 400, 300);
		console = new ServerConsole(this);

		gameData = new SimpleGameData();
		networkServer = new KryonetGameServer(Globals.GAME_PORT, Globals.GAME_PORT, this);

		physicsController = new SimplePhysicsController<PhysicalEntity>(this);
	}


	@Override
	public void simpleInitApp() {
		createGame();
		console.appendText("Game created");
		
		try {
			clientToLobbyServer = new KryonetLobbyClient(gameOptions.lobbyip, gameOptions.lobbyport, gameOptions.lobbyport, this);
		} catch (IOException e) {
			Globals.p("Unable to connect to lobby server");
			//throw new RuntimeException(e.getMessage());
		}

		loopTimer.start();
	}


	protected abstract void createGame();


	@Override
	public void simpleUpdate(float tpf_secs) {
		StringBuilder strDebug = new StringBuilder();
		
		if (updateLobbyInterval.hitInterval() && clientToLobbyServer != null) {
			this.clientToLobbyServer.sendMessageToServer(new UpdateLobbyMessage(gameOptions.displayName, gameOptions.ourExternalIP, gameOptions.ourExternalPort, this.clients.size(), true)); // todo - do we have spaces?
		}

		if (networkServer.getNumClients() > 0) {
			// Process all messages
			synchronized (unprocessedMessages) {
				while (!this.unprocessedMessages.isEmpty()) {
					MyAbstractMessage message = this.unprocessedMessages.remove(0);
					ClientData client = message.client;

					if (message instanceof NewPlayerRequestMessage) {
						this.playerJoined(client, message);

					} else if (message instanceof UnknownEntityMessage) {
						UnknownEntityMessage uem = (UnknownEntityMessage) message;
						IEntity e = null;
						synchronized (entities) {
							e = this.entities.get(uem.entityID);
						}
						this.sendNewEntity(client, e);

					} else if (message instanceof PlayerLeftMessage) {
						this.connectionRemoved(client.getPlayerID());

					} else if (message instanceof PlayerInputMessage) { // Process these here so the inputs don't change values mid-thread
						PlayerInputMessage pim = (PlayerInputMessage)message;
						if (pim.timestamp > client.latestInputTimestamp) {
							client.remoteInput.decodeMessage(pim);
							client.latestInputTimestamp = pim.timestamp;
						}

					} else if (message instanceof RequestNewBulletMessage) {
						RequestNewBulletMessage rnbe = (RequestNewBulletMessage) message;
						IRequiresAmmoCache irac = (IRequiresAmmoCache)this.entities.get(rnbe.ownerEntityID);
						IEntity e = this.createEntity(rnbe.type, getNextEntityID(), -1, irac);
						//irac.addToCache(e);

					} else {
						throw new RuntimeException("Unknown message type: " + message);
					}
				}
			}

			if (sendPingInterval.hitInterval()) {
				this.networkServer.sendMessageToAll(new PingMessage(true));
			}

			synchronized (this.clients) {
				// If any avatars are shooting a gun the requires "rewinding time", rewind all avatars and calc the hits all together to save time
				boolean areAnyPlayersShooting = false;
				for (ClientData c : this.clients.values()) {
					AbstractServerAvatar avatar = c.avatar;
					if (avatar != null && avatar.isShooting() && avatar.abilityGun instanceof ICalcHitInPast) {
						areAnyPlayersShooting = true;
						break;
					}
				}
				if (areAnyPlayersShooting) {
					long timeTo = System.currentTimeMillis() - Globals.CLIENT_RENDER_DELAY;
					this.rewindEntities(timeTo);
					this.rootNode.updateGeometricState();
					for (ClientData c : this.clients.values()) {
						AbstractServerAvatar avatar = c.avatar;
						if (avatar != null && avatar.isShooting() && avatar.abilityGun instanceof ICalcHitInPast) {
							ICalcHitInPast chip = (ICalcHitInPast) avatar.abilityGun;
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
				// Add and remove entities
				for(IEntity e : this.toAdd) {
					this.actuallyAddEntity(e);
				}
				this.toAdd.clear();

				for(Integer i : this.toRemove) {
					this.actuallyRemoveEntity(i);
				}
				this.toRemove.clear();

				// Loop through the entities
				for (IEntity e : entities.values()) {
					if (e instanceof IPreprocess) {
						IPreprocess p = (IPreprocess)e;
						p.preprocess();
					}

					if (e instanceof IProcessByServer) {
						IProcessByServer p = (IProcessByServer)e;
						p.processByServer(this, tpf_secs);
					}

					if (e instanceof PhysicalEntity) {
						PhysicalEntity physicalEntity = (PhysicalEntity)e;
						strDebug.append(e.getID() + ": " + e.getName() + " Pos: " + physicalEntity.getWorldTranslation() + "\n");
						/*if (sc.type == EntityTypes.AVATAR) {
							AbstractPlayersAvatar av = (AbstractPlayersAvatar)sc;
							strDebug.append("WalkDir: " + av.playerControl.getWalkDirection() + "   Velocity: " + av.playerControl.getVelocity().length() + "\n");
						}*/
						if (sendUpdates) {
							if (physicalEntity.hasMoved()) { // Don't send if not moved (unless Avatar)
								eum.addEntityData(physicalEntity, false);
							}
						}
					} else {
						strDebug.append(e.getID() + ": " + e.getName() + "\n");
					}
				}
			}
			if (sendUpdates) {
				for (ClientData client : this.clients.values()) {
					if (client.clientStatus == ClientStatus.Accepted) {
						networkServer.sendMessageToClient(client, eum);	
					}
				}
				//networkServer.sendMessageToAll(eum);
			}
			if (checkStatusInterval.hitInterval()) {
				this.checkGameStatus(false);
			}
		}

		this.logWindow.setText(strDebug.toString());

		loopTimer.waitForFinish(); // Keep clients and server running at same speed
		loopTimer.start();
	}

	
	protected void playerJoined(ClientData client, MyAbstractMessage message) {
		NewPlayerRequestMessage newPlayerMessage = (NewPlayerRequestMessage) message;
		int side = getSide(client);
		client.playerData = new SimplePlayerData(client.id, newPlayerMessage.name, side);
		networkServer.sendMessageToClient(client, new GameSuccessfullyJoinedMessage(client.getPlayerID(), side));//, client.avatar.id)); // Must be before we send the avatar so they know it's their avatar
		client.avatar = createPlayersAvatar(client, side);
		sendAllEntitiesToClient(client);
		client.clientStatus = ClientData.ClientStatus.Accepted;

		this.sendGameStatusMessage();
		this.networkServer.sendMessageToClient(client, new PingMessage(true));

		checkGameStatus(true);

	}


	private void checkGameStatus(boolean playersChanged) {
		int oldStatus = gameData.getGameStatus();
		if (playersChanged) {
			boolean enoughPlayers = areThereEnoughPlayers();
			if (!enoughPlayers && gameData.isInGame()) {
				gameData.setGameStatus(SimpleGameData.ST_WAITING_FOR_PLAYERS, 0);
			} else if (enoughPlayers && gameData.getGameStatus() == SimpleGameData.ST_WAITING_FOR_PLAYERS) {
				gameData.setGameStatus(SimpleGameData.ST_DEPLOYING, gameOptions.deployDuration);
			}
		}

		long duration = System.currentTimeMillis() - gameData.statusStartTimeMS;
		if (gameData.getGameStatus() == SimpleGameData.ST_DEPLOYING) {
			if (duration >= this.gameOptions.deployDuration) {
				gameData.setGameStatus(SimpleGameData.ST_STARTED, gameOptions.gameDuration);
			}
		} else if (gameData.getGameStatus() == SimpleGameData.ST_STARTED) {
			if (duration >= this.gameOptions.gameDuration) {
				gameData.setGameStatus(SimpleGameData.ST_FINISHED, gameOptions.finishedDuration);
			}
		} else if (gameData.getGameStatus() == SimpleGameData.ST_FINISHED) {
			if (duration >= this.gameOptions.finishedDuration) {
				gameData.setGameStatus(SimpleGameData.ST_DEPLOYING, gameOptions.deployDuration);
			}
		}
		if (oldStatus != gameData.getGameStatus()) {
			sendGameStatusMessage();
		}
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


	private boolean areThereEnoughPlayers() {
		/*if (this.gameOptions.areAllPlayersOnDifferentSides()) {
			return clients.values().size() >= 2;
		} else {*/
		ArrayList<Integer> map = new ArrayList<Integer>();
		for (ClientData client : this.clients.values()) {
			if (client.avatar != null) {
				if (!map.contains(client.avatar.side)) {
					map.add(client.avatar.side);
				}
			}
		}
		return map.size() >= 2;
		//}
	}


	private HashMap<Integer, Integer> getPlayersPerSide() {
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (ClientData client : this.clients.values()) {
			if (client.avatar != null) {
				if (!map.containsKey(client.avatar.side)) {
					map.put(client.avatar.side, 0);
				}
				int val = map.get(client.avatar.side);
				val++;
				map.put(client.avatar.side, val);
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
					long rttDuration = System.currentTimeMillis() - pingMessage.originalSentTime;
					client.playerData.pingRTT = client.pingCalc.add(rttDuration);
					client.serverToClientDiffTime = pingMessage.responseSentTime - pingMessage.originalSentTime - (client.playerData.pingRTT/2); // If running on the same server, this should be 0! (or close enough)
					//Settings.p("Client rtt = " + client.pingRTT);
					//Settings.p("serverToClientDiffTime = " + client.serverToClientDiffTime);
					if ((client.playerData.pingRTT/2) + Globals.SERVER_SEND_UPDATE_INTERVAL_MS > Globals.CLIENT_RENDER_DELAY) {
						Globals.p("Warning: client ping is longer than client render delay!");
					}
				} catch (NullPointerException npe) {
					npe.printStackTrace();
				}
			} else {
				// Send it back to the client
				pingMessage.responseSentTime = System.currentTimeMillis();
				this.networkServer.sendMessageToClient(client, pingMessage);
			}

		} else {
			msg.client = client;
			// Add it to list for processing in main thread
			synchronized (this.unprocessedMessages) {
				this.unprocessedMessages.add(msg);
			}
		}

	}


	private AbstractServerAvatar createPlayersAvatar(ClientData client, int side) {
		int id = getNextEntityID();
		AbstractServerAvatar avatar = this.createPlayersAvatarEntity(client, id, side);
		avatar.setWorldTranslation(this.getAvatarStartPosition(avatar));
		//avatar.moveToStartPostion(true);
		this.addEntity(avatar);

		this.equipAvatar(avatar);
		/*
		IAbility abilityGun = new HitscanRifle(this, getNextEntityID(), avatar, 0);
		//IAbility abilityGun = new GrenadeLauncher(this, getNextEntityID(), avatar, 0);
		this.addEntity(abilityGun);

		/* 
			this.abilityOther = new JetPac(this, 1);// BoostFwd(this, 1);//getRandomAbility(this);
		game.addEntity(abilityOther);
		}*/

		return avatar;
	}


	protected abstract AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid, int side);

	protected abstract void equipAvatar(AbstractServerAvatar avatar);

	protected IEntity createEntity(int type, int entityid, int side, IRequiresAmmoCache irac) {
		switch (type) {
		default:
			throw new RuntimeException("Unknown entity type: " + type);
			//return super.createEntity();
		}
	}


	private void sendAllEntitiesToClient(ClientData client) {
		synchronized (entities) {
			for (IEntity e : entities.values()) {
				this.sendNewEntity(client, e);
			}
			GeneralCommandMessage aes = new GeneralCommandMessage(GeneralCommandMessage.Command.AllEntitiesSent);
			this.networkServer.sendMessageToClient(client, aes);
		}
	}


	private void sendNewEntity(ClientData client, IEntity e) {
		if (e instanceof PhysicalEntity) {
			PhysicalEntity se = (PhysicalEntity)e;
			NewEntityMessage nem = new NewEntityMessage(se);
			this.networkServer.sendMessageToClient(client, nem);
		}
	}


	@Override
	public void connectionAdded(int id, Object net) {
		Globals.p("Client connected!");
		ClientData client = new ClientData(id, net, this.getCamera(), this.getInputManager());
		synchronized (clients) {
			clients.put(id, client);
		}
		this.networkServer.sendMessageToClient(client, new WelcomeClientMessage());
	}


	@Override
	public void connectionRemoved(int id) {
		Globals.p("connectionRemoved()");
		synchronized (clients) {
			ClientData client = clients.get(id);
			if (client != null) { // For some reason, connectionRemoved() gets called multiple times
				this.playerLeft(client);
			}
		}
	}


	protected void playerLeft(ClientData client) {
		Globals.p("Removing player " + client.getPlayerID());
		synchronized (clients) {
			this.clients.remove(client.getPlayerID());
		}
		// Remove avatar
		if (client.avatar != null) {
			this.removeEntity(client.avatar.id);
			//gameData.removePlayer(client);
		}
		this.sendGameStatusMessage();
		checkGameStatus(true);
	}


	@Override
	public void addEntity(IEntity e) {
		this.toAdd.add(e);
	}


	public void actuallyAddEntity(IEntity e) {
		synchronized (entities) {
			//Settings.p("Trying to add " + e + " (id " + e.getID() + ")");
			if (this.entities.containsKey(e.getID())) {
				throw new RuntimeException("Entity id " + e.getID() + " already exists: " + e);
			}
			this.entities.put(e.getID(), e);

			// Tell clients
			NewEntityMessage nem = new NewEntityMessage(e);
			for (ClientData client : this.clients.values()) {
				if (client.clientStatus == ClientStatus.Accepted) {
					networkServer.sendMessageToClient(client, nem);	
				}
			}
			this.console.appendText("Created " + e);
		}

	}


	@Override
	public void removeEntity(int id) {
		this.toRemove.add(id);
	}


	private void actuallyRemoveEntity(int id) {
		synchronized (entities) {
			IEntity e = this.entities.get(id);
			Globals.p("Removing entity " + e.getName() + " / ID:" + id);
			if (e instanceof PhysicalEntity) {
				PhysicalEntity pe = (PhysicalEntity)e;
				this.physicsController.removeSimpleRigidBody(pe.simpleRigidBody);
			}
			this.entities.remove(id);
			this.console.appendText("Removed " + e);
		}
		this.networkServer.sendMessageToAll(new RemoveEntityMessage(id));
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
		} else if (cmd.equals("restart")) {
			restartGame();
		} else if (cmd.equals("quit")) {
			this.networkServer.close();
			this.stop();
		}
	}


	private void restartGame() {
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

		this.getRootNode().detachAllChildren();
		this.getPhysicsController().removeAllEntities();

		gameData.setGameStatus(SimpleGameData.ST_WAITING_FOR_PLAYERS, 0);
	}

/*
	@Override
	public Type getJmeContext() {
		return getContext().getType();
	}

*/
	public ArrayList<RayCollisionData> checkForEntityCollisions(Ray r) {
		//return this.physicsController.checkForCollisions(r);

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
		/*if (a.userObject instanceof Floor == false && b.userObject instanceof Floor == false) {
			Globals.p("Collision between " + a.userObject + " and " + b.userObject);
		}*/
		//if (a != null && b != null) {
		collisionLogic.collision(a.userObject,  b.userObject);
		/*} else {
			Settings.p("null object in collision");
		}*/
		if (a.userObject instanceof INotifiedOfCollision) {
			INotifiedOfCollision ic = (INotifiedOfCollision)a.userObject;
			ic.collided(b.userObject);
		}
		if (b.userObject instanceof INotifiedOfCollision) {
			INotifiedOfCollision ic = (INotifiedOfCollision)b.userObject;
			ic.collided(a.userObject);
		}
		
	}


	@Override
	public SimplePhysicsController<PhysicalEntity> getPhysicsController() {
		return physicsController;
	}


	private void sendGameStatusMessage() {
		ArrayList<SimplePlayerData> players = new ArrayList<SimplePlayerData>();
		for(ClientData client : this.clients.values()) {
			players.add(client.playerData);
		}
		this.networkServer.sendMessageToAll(new GameStatusMessage(this.gameData, players));

	}


	public abstract Vector3f getAvatarStartPosition(AbstractAvatar avatar);


	@Override
	public void connected() {
		Globals.p("Connected to lobby server");
		
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

}

