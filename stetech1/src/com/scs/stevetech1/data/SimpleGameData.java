package com.scs.stevetech1.data;

import com.jme3.network.serializing.Serializable;

/*
 * This should only contain stuff that is completely replaced when a new "mission" starts.
 */
@Serializable
public class SimpleGameData { // pojo

	public static final int ST_WAITING_FOR_PLAYERS = 0;
	public static final int ST_DEPLOYING = 1; // Players waiting in spawn area 
	public static final int ST_STARTED = 2; // Players released!
	public static final int ST_FINISHED = 3;

	private int gameStatus = ST_WAITING_FOR_PLAYERS;
	private long statusStartTimeMS;
	private long statusDurationMS;

	public SimpleGameData() {
		super();
		
		statusStartTimeMS = System.currentTimeMillis();

	}


	public int getGameStatus() {
		return gameStatus;
	}

	
	public static String getStatusDesc(int s) {
		switch (s) {
		case ST_WAITING_FOR_PLAYERS: return "Waiting for players";
		case ST_DEPLOYING: return "Deploying";
		case ST_STARTED: return "Started";
		case ST_FINISHED: return "Finished";
		default: throw new RuntimeException("Unknown status: " + s);
		}
	}

	
	public boolean isInGame() {
		return gameStatus == SimpleGameData.ST_DEPLOYING || gameStatus == SimpleGameData.ST_STARTED;
	}

	
	public long getStatusStartTimeMS() {
		return statusStartTimeMS;
	}
	

	/*
	 * Only called by the server
	 */
	public void setGameStatus(int newStatus, long duration) {
		if (gameStatus != newStatus) {
			gameStatus = newStatus;
			statusStartTimeMS = System.currentTimeMillis();
			statusDurationMS = duration;
			//server.gameStatusChanged(newStatus);
		}
	}
	
	
	public String getTime(long now) {
		switch (this.gameStatus) {
		case ST_WAITING_FOR_PLAYERS: 
			return (now-statusStartTimeMS)/1000 + " seconds";
		case ST_DEPLOYING: 
		case ST_STARTED: 
		case ST_FINISHED:
			long endTime = statusStartTimeMS + statusDurationMS;
			return (endTime-now)/1000 + " seconds";
		default: 
			throw new RuntimeException("Unknown status: " + gameStatus);
		}
	}

}
