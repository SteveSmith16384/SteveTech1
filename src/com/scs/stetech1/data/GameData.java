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
	private int maxPlayersPerSide;
	private int maxSides;
	private int status;
	public String name;
	public long statusStartTime;
	public ArrayList<ClientData> players = new ArrayList<ClientData>();
	
	public GameData(AbstractGameServer _server, int _maxPlayersPerSide, int _maxSides) {
		super();
		
		server = _server;
		maxPlayersPerSide = _maxPlayersPerSide;
		maxSides = _maxSides;
	}

	
	public void checkGameStatus() {
		// todo
	}
	
	
	public void setName(String n) {			
		name = n;
	}


	public int getStatus() {
		return status;
	}
	
	
	public void setGameStatus(int newStatus) {
		if (status != newStatus) {
			status = newStatus;
			//if (status == ST_WAITING_FOR_PLAYERS && newStatus == GameStatus.Started) {
				statusStartTime = System.currentTimeMillis();
			//}
			server.gameStatusChanged(newStatus);
		}
	}
	
	
	public void addPlayer(ClientData clientData) {
		players.add(clientData);
	}
	

	public void removePlayer(ClientData clientData) {
		players.remove(clientData);
	}
	
}
