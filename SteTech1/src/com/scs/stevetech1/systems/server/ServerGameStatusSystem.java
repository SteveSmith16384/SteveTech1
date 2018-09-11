package com.scs.stevetech1.systems.server;

import java.util.ArrayList;

import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;

public class ServerGameStatusSystem {

	private AbstractGameServer server;
	//private long endOfCurrentStatus = -1;

	public ServerGameStatusSystem(AbstractGameServer _server) {
		super();

		server = _server;
	}


	public void checkGameStatus(boolean playersChanged) {
		if (Globals.DEBUG_GAME_STATUS_CHECK) {
			Globals.p("Checking game status.");
		}
		
		SimpleGameData gameData = server.getGameData();

		if (playersChanged) {
			boolean enoughPlayers = areThereEnoughPlayers();
			if (Globals.DEBUG_GAME_NOT_STARTING) {
				Globals.p("Checking game status.  enoughPlayers=" + enoughPlayers);
			}
			if (!enoughPlayers && gameData.isInGame()) {
				this.setGameStatus(SimpleGameData.ST_WAITING_FOR_PLAYERS);
			} else if (enoughPlayers && gameData.getGameStatus() == SimpleGameData.ST_WAITING_FOR_PLAYERS) {
				this.setGameStatus(SimpleGameData.ST_DEPLOYING);
			}
		}

		long currentDuration = System.currentTimeMillis() - gameData.getStatusStartTimeMS();
		if (gameData.getGameStatus() == SimpleGameData.ST_WAITING_FOR_PLAYERS) {
			// Do nothing...
		} else if (gameData.getGameStatus() == SimpleGameData.ST_DEPLOYING) {
			if (currentDuration >= gameData.getStatusDuration()) {
				this.setGameStatus(SimpleGameData.ST_STARTED);
			}
		} else if (gameData.getGameStatus() == SimpleGameData.ST_STARTED) {
			if (currentDuration >= gameData.getStatusDuration()) {
				this.setGameStatus(SimpleGameData.ST_FINISHED);
			}
		} else if (gameData.getGameStatus() == SimpleGameData.ST_FINISHED) {
			if (currentDuration >= gameData.getStatusDuration()) {
				this.setGameStatus(SimpleGameData.ST_DEPLOYING);
			}
		} else {
			throw new RuntimeException("Unknown game status: " + gameData.getGameStatus());
		}

	}


	public void setGameStatus(int status) {
		SimpleGameData gameData = server.getGameData();
		GameOptions gameOptions = server.gameOptions;

		int oldStatus = gameData.getGameStatus();
		if (oldStatus != status) {
			switch (status) {
			case SimpleGameData.ST_WAITING_FOR_PLAYERS:
				gameData.setGameStatus(SimpleGameData.ST_WAITING_FOR_PLAYERS, 0);
				break;
			case SimpleGameData.ST_DEPLOYING:
				gameData.setGameStatus(SimpleGameData.ST_DEPLOYING, gameOptions.deployDurationMillis);
				break;
			case SimpleGameData.ST_STARTED:
				gameData.setGameStatus(SimpleGameData.ST_STARTED, gameOptions.gameDurationMillis);
				break;
			case SimpleGameData.ST_FINISHED:
				gameData.setGameStatus(SimpleGameData.ST_FINISHED, gameOptions.finishedDurationMillis);
				break;
			default:
				throw new RuntimeException("Invalid status: " + status);
			}
			server.sendSimpleGameDataToClients();

			if (oldStatus != gameData.getGameStatus()) {
				server.gameStatusChanged(gameData.getGameStatus());
			}
			//Globals.p("New game status: " + gameData.getGameStatus());
		}
	}

	private boolean areThereEnoughPlayers() {
		ArrayList<Byte> map = new ArrayList<Byte>();
		for (ClientData client : server.clientList.getClients()) {
			if (client.avatar != null) {
				if (!map.contains(client.getSide())) {
					map.add(client.getSide());
				}
			}
		}
		return map.size() >= server.getMinPlayersRequiredForGame();
	}


}
