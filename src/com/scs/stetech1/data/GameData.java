package com.scs.stetech1.data;

import java.util.ArrayList;

import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.ClientData;

/*
 * This should only contain stuff that is completely replaced when a new "mission" starts.
 */
public class GameData {

	//public enum GameStatus {WaitingForPlayers, Started, Finished }
	public static final int ST_WAITING_FOR_PLAYERS = 0;
	public static final int ST_DEPLOYING = 1;
	public static final int ST_STARTED = 2;
	public static final int ST_FINISHED = 3;

	private AbstractGameServer server;
	private int status;
	public long statusStartTime;

	public GameData(AbstractGameServer _server) {
		super();

		server = _server;
	}


	public int getStatus() {
		return status;
	}

	
	public static String getStatusDesc(int s) {
		switch (s) {
		case ST_WAITING_FOR_PLAYERS: return "Waiting for players";
		case ST_DEPLOYING: return "Deploying";
		case ST_STARTED: return "Started";
		case ST_FINISHED: return "Started";
		default: throw new RuntimeException("Unknown status: " + s);
		}
	}

	
	public boolean isInGame() {
		return status == GameData.ST_DEPLOYING || status == GameData.ST_STARTED;
	}


	public void setGameStatus(int newStatus) {
		if (status != newStatus) {
			status = newStatus;
			statusStartTime = System.currentTimeMillis();
			server.gameStatusChanged(newStatus);
		}
	}

}
