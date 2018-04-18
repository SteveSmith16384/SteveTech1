package com.scs.stevetech1.systems.server;

import java.util.ArrayList;

import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;

public class ServerGameStatusSystem {
	
	private AbstractGameServer server;
	
	public ServerGameStatusSystem(AbstractGameServer _server) {
		super();
		
		server = _server;
	}

	
	public void checkGameStatus(boolean playersChanged) {
		SimpleGameData gameData = server.gameData;
		GameOptions gameOptions = server.gameOptions;
		
		int oldStatus = gameData.getGameStatus();
		if (playersChanged) {
			boolean enoughPlayers = areThereEnoughPlayers();
			if (!enoughPlayers && gameData.isInGame()) {
				gameData.setGameStatus(SimpleGameData.ST_WAITING_FOR_PLAYERS, 0);
			} else if (enoughPlayers && gameData.getGameStatus() == SimpleGameData.ST_WAITING_FOR_PLAYERS) {
				gameData.setGameStatus(SimpleGameData.ST_DEPLOYING, gameOptions.deployDurationMillis);
			}
		}

		long currentDuration = System.currentTimeMillis() - gameData.getStatusStartTimeMS();
		if (gameData.getGameStatus() == SimpleGameData.ST_WAITING_FOR_PLAYERS) {
			// Do nothing...
		} else if (gameData.getGameStatus() == SimpleGameData.ST_DEPLOYING) {
			if (currentDuration >= gameData.getStatusDuration()) {
					gameData.setGameStatus(SimpleGameData.ST_STARTED, gameOptions.gameDurationMillis);
				}
		} else if (gameData.getGameStatus() == SimpleGameData.ST_STARTED) {
			if (currentDuration >= gameData.getStatusDuration()) {
				gameData.setGameStatus(SimpleGameData.ST_FINISHED, gameOptions.finishedDurationMillis);
			}
		} else if (gameData.getGameStatus() == SimpleGameData.ST_FINISHED) {
			if (currentDuration >= gameData.getStatusDuration()) {
				gameData.setGameStatus(SimpleGameData.ST_DEPLOYING, gameOptions.deployDurationMillis);
			}
		} else {
			throw new RuntimeException("Unknown game status: " + gameData.getGameStatus());
		}
		
		server.sendGameStatusMessage();
		
		if (oldStatus != gameData.getGameStatus()) {
			server.gameStatusChanged(gameData.getGameStatus());
		}
	}

	
	private boolean areThereEnoughPlayers() {
		ArrayList<Integer> map = new ArrayList<Integer>();
		for (ClientData client : server.clients.values()) {
			if (client.avatar != null) {
				if (!map.contains(client.side)) {
					map.add(client.side);
				}
			}
		}
		return map.size() >= server.getMinPlayersRequiredForGame();
	}


}
