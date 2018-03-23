package com.scs.stevetech1.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IGetReadyForGame;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.data.SimplePlayerData;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.lobby.KryonetLobbyClient;
import com.scs.stevetech1.netmessages.GameOverMessage;
import com.scs.stevetech1.netmessages.GenericStringMessage;
import com.scs.stevetech1.netmessages.ModelBoundsMessage;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.netmessages.PingMessage;
import com.scs.stevetech1.netmessages.SimpleGameDataMessage;
import com.scs.stevetech1.netmessages.lobby.UpdateLobbyMessage;
import com.scs.stevetech1.server.ClientData.ClientStatus;
import com.scs.stevetech1.systems.server.ServerGameStatusSystem;
import com.scs.stevetech1.systems.server.ServerPingSystem;

import ssmith.util.ConsoleInputListener;
import ssmith.util.RealtimeInterval;
import ssmith.util.TextConsole;


/**
 * This extends the AbstractEntityServer to give it the concept of a game.
 *
 */
public abstract class AbstractGameServer extends AbstractEntityServer implements ConsoleInputListener {

	protected static AtomicInteger nextGameID = new AtomicInteger(1);

	private KryonetLobbyClient clientToLobbyServer;

	private RealtimeInterval updateLobbyInterval = new RealtimeInterval(30 * 1000);
	private RealtimeInterval checkGameStatusInterval = new RealtimeInterval(5000);
	public SimpleGameData gameData;

	// Systems
	private ServerGameStatusSystem gameStatusSystem;
	private ServerPingSystem pingSystem;

	public AbstractGameServer(String gameID, GameOptions _gameOptions, int _tickrateMillis, int sendUpdateIntervalMillis, int _clientRenderDelayMillis, int _timeoutMillis, float gravity, float aerodynamicness) throws IOException {
		super(gameID, _gameOptions, _tickrateMillis, sendUpdateIntervalMillis, _clientRenderDelayMillis, _timeoutMillis, gravity, aerodynamicness);

	}


	@Override
	public void simpleInitApp() {
		super.simpleInitApp();

		gameData = new SimpleGameData();
		this.gameStatusSystem = new ServerGameStatusSystem(this);
		this.pingSystem = new ServerPingSystem(this);

		// Start console
		new TextConsole(this);

		startNewGame();
	}


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
		long startTime = System.currentTimeMillis();

		super.simpleUpdate(tpf_secs);

		if (gameOptions.lobbyip != null) {
			if (updateLobbyInterval.hitInterval()) {
				if (clientToLobbyServer == null) {
					connectToLobby();
				}
				if (clientToLobbyServer != null) {
					boolean spaces = this.doWeHaveSpaces();
					this.clientToLobbyServer.sendMessageToServer(new UpdateLobbyMessage(gameOptions.displayName, gameOptions.ourExternalIP, gameOptions.ourExternalPort, this.clients.size(), spaces));
				}
			}
		}

		if (checkGameStatusInterval.hitInterval()) {
			gameStatusSystem.checkGameStatus(false);
		}

		this.pingSystem.process();

		if (Globals.PROFILE_SERVER) {
			long endTime = System.currentTimeMillis();
			long diff = endTime - startTime;
			Globals.p("Num entities to loop through: " + this.entitiesForProcessing.size());
			Globals.p("Server loop took " + diff);
		}

		loopTimer.waitForFinish(); // Keep clients and server running at same speed
		loopTimer.start();
	}


	@Override
	public void messageReceived(int clientid, MyAbstractMessage message) {
		if (message instanceof PingMessage) {
			PingMessage pingMessage = (PingMessage) message;

			ClientData client = null;
			synchronized (clients) {
				client = clients.get(clientid);
			}

			if (client == null) {
				return;
			}

			this.pingSystem.handleMessage(pingMessage, client);

		} else {
			super.messageReceived(clientid, message);
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


	/*
	public ArrayList<RayCollisionData> checkForEntityCollisions_UNUSED(Ray r) { // todo - is this use?
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
	 */

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
	public void disconnected() {
		Globals.p("Disconnected from lobby server");

	}


	@Override
	public Node getGameNode() {
		return rootNode;
	}


	public void gameStatusChanged(int newStatus)  {
		if (newStatus == SimpleGameData.ST_DEPLOYING) {
			gameData.gameID++;
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
		Globals.p("Num Entities for proc: " + this.entitiesForProcessing.size());
		Globals.p("Num Clients: " + this.clients.size());
	}


	protected void playerLeft(ClientData client) {
		super.playerLeft(client);

		this.gameNetworkServer.sendMessageToAllExcept(client, new GenericStringMessage("Player joined!", true));
		this.sendGameStatusMessage();
		gameStatusSystem.checkGameStatus(true);
	}


	protected synchronized void playerConnected(ClientData client, MyAbstractMessage message) {
		super.playerConnected(client, message);

		this.sendGameStatusMessage();

		this.pingSystem.sendPingToClient(client);
		this.gameNetworkServer.sendMessageToAllExcept(client, new GenericStringMessage("Player joined!", true));

		playerJoinedGame(client);

		gameStatusSystem.checkGameStatus(true);

	}


}

