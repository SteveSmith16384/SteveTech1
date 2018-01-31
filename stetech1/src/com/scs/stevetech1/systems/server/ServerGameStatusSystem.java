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

		long duration = System.currentTimeMillis() - gameData.statusStartTimeMS;
		if (gameData.getGameStatus() == SimpleGameData.ST_DEPLOYING) {
			if (duration >= gameOptions.deployDurationMillis) {
				gameData.setGameStatus(SimpleGameData.ST_STARTED, gameOptions.gameDurationMillis);
			}
		} else if (gameData.getGameStatus() == SimpleGameData.ST_STARTED) {
			if (duration >= gameOptions.gameDurationMillis) {
				gameData.setGameStatus(SimpleGameData.ST_FINISHED, gameOptions.finishedDurationMillis);
			}
		} else if (gameData.getGameStatus() == SimpleGameData.ST_FINISHED) {
			if (duration >= gameOptions.finishedDurationMillis) {
				gameData.setGameStatus(SimpleGameData.ST_DEPLOYING, gameOptions.deployDurationMillis);
			}
		}
		if (oldStatus != gameData.getGameStatus()) {
			server.sendGameStatusMessage();
		}
	}

	
	private boolean areThereEnoughPlayers() {
		/*if (this.gameOptions.areAllPlayersOnDifferentSides()) {
			return clients.values().size() >= 2;
		} else {*/
		ArrayList<Integer> map = new ArrayList<Integer>();
		for (ClientData client : server.clients.values()) {
			if (client.avatar != null) {
				if (!map.contains(client.avatar.side)) {
					map.add(client.avatar.side);
				}
			}
		}
		return map.size() >= 2;
		//}
	}





}
