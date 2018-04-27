package com.scs.stevetech1.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IGetReadyForGame;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.data.SimplePlayerData;
import com.scs.stevetech1.entities.AbstractServerAvatar;
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
public abstract class AbstractGameServer extends AbstractEntityServer implements ConsoleInputListener { // todo - move code to AbstractEntityServer

	public AbstractGameServer(String gameID, GameOptions _gameOptions, int _tickrateMillis, int sendUpdateIntervalMillis, int _clientRenderDelayMillis, int _timeoutMillis) throws IOException { // , float gravity, float aerodynamicness
		super(gameID, _gameOptions, _tickrateMillis, sendUpdateIntervalMillis, _clientRenderDelayMillis, _timeoutMillis);//, gravity, aerodynamicness);

	}


	@Override
	public void simpleInitApp() {
		super.simpleInitApp();

		// Start console
		new TextConsole(this);

		startNewGame();
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

	@Override
	public void simpleUpdate(float tpf_secs) {
		long startTime = System.currentTimeMillis();

		super.simpleUpdate(tpf_secs); //this.entities

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

		if (Globals.PROFILE_SERVER) {
			long endTime = System.currentTimeMillis();
			long diff = endTime - startTime;
			Globals.p("Num entities to loop through: " + this.entitiesForProcessing.size());
			Globals.p("Server loop took " + diff);
		}

		loopTimer.waitForFinish(); // Keep clients and server running at same speed
		loopTimer.start();
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
		Globals.p("Num Entities: " + this.entities.size());
		Globals.p("Num Entities for proc: " + this.entitiesForProcessing.size());
		Globals.p("Num Clients: " + this.clients.size());
	}


	public void playerKilled(AbstractServerAvatar avatar) {
		// Override if req
	}
	
	
}

