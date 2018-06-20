package com.scs.stevetech1.data;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.server.Globals;

/*
 * This should only contain stuff that is completely replaced when a new game starts.
 * No!  We need to keep track of status and duration when going from waitingForPlayers to Deployment
 */
@Serializable
public final class SimpleGameData { // POJO

	// Game statuses
	public static final int ST_WAITING_FOR_PLAYERS = 0;
	public static final int ST_DEPLOYING = 2; // Players typically waiting in spawn area 
	public static final int ST_STARTED = 3; // Players released!
	public static final int ST_FINISHED = 4;

	public int gameID = -1;
	private int gameStatus = ST_WAITING_FOR_PLAYERS;
	private long statusStartTimeMS;
	private long statusEndTimeMS;
	private long statusDurationMS;

	public SimpleGameData() { // For Kryo
		super();
		
		statusStartTimeMS = System.currentTimeMillis();
		statusEndTimeMS = Long.MAX_VALUE;

	}


	public SimpleGameData(int _gameID) {
		super();
		
		gameID = _gameID;
		statusStartTimeMS = System.currentTimeMillis();
		statusEndTimeMS = Long.MAX_VALUE;

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
	

	public long getStatusEndTimeMS() {
		return statusEndTimeMS;
	}
	

	/*
	 * Only called by the server
	 */
	public void setGameStatus(int newStatus, long duration) {
		if (gameStatus != newStatus) {
			gameStatus = newStatus;
			statusStartTimeMS = System.currentTimeMillis();
			if (duration > 0) {
				statusEndTimeMS = System.currentTimeMillis() + duration;
			} else {
				statusEndTimeMS = Long.MAX_VALUE;
			}
			statusDurationMS = duration;
			//server.gameStatusChanged(newStatus);
			
			if (Globals.DEBUG_GAME_NOT_STARTING) {
				Globals.p("Game " + this.gameID + " status now " + getStatusDesc(this.gameStatus));
			}
		} else {
			if (Globals.DEBUG_GAME_NOT_STARTING) {
				Globals.p("Game " + this.gameID + " status remains " + getStatusDesc(newStatus));
			}
		}
	}
	
	
	public String getTime(long now) {
		switch (this.gameStatus) {
		case ST_WAITING_FOR_PLAYERS: 
			return (now-statusStartTimeMS)/1000 + " seconds remaining";
		//case ST_CLEAR_OLD_GAME:
		case ST_DEPLOYING:
		case ST_STARTED: 
		case ST_FINISHED:
			long endTime = statusStartTimeMS + statusDurationMS;
			return (endTime-now)/1000 + " seconds remaining";
		default: 
			throw new RuntimeException("Unknown status: " + gameStatus);
		}
	}
	
	
	public long getStatusDuration() {
		return this.statusDurationMS;
	}

}
