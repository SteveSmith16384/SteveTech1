package com.scs.stetech1.data;

import com.jme3.network.serializing.Serializable;

/*
 * This should only contain stuff that is completely replaced when a new "mission" starts.
 */
@Serializable
public class SimpleGameData { // pojo

	public static final int ST_WAITING_FOR_PLAYERS = 0;
	public static final int ST_DEPLOYING = 1;
	public static final int ST_STARTED = 2;
	public static final int ST_FINISHED = 3;

	private int gameStatus = ST_WAITING_FOR_PLAYERS;
	public long statusStartTime, statusEndTime; // todo - use statusEndTime

	public SimpleGameData() {
		super();

	}


	public int getGameStatus() {
		return gameStatus;
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
		return gameStatus == SimpleGameData.ST_DEPLOYING || gameStatus == SimpleGameData.ST_STARTED;
	}


	public void setGameStatus(int newStatus) {
		if (gameStatus != newStatus) {
			gameStatus = newStatus;
			statusStartTime = System.currentTimeMillis();
			//server.gameStatusChanged(newStatus);
		}
	}
	
	
	public String getTime() {
		return "todo"; // show either time left or time going
	}

}
