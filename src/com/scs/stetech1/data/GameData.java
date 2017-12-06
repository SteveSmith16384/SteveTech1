package com.scs.stetech1.data;

import java.util.ArrayList;

import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.ClientData;

/*
 * This should only contain stuff that is completely replaced when a new "mission" starts.
 */
public class GameData {

	public enum GameStatus {WaitingForPlayers, Started, Finished } // todo - create statusChanged listener

	private AbstractGameServer server;
	private int maxPlayersPerSide;
	private int maxSides;
	private GameStatus status;
	public String name;
	private long gameStartTime;
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


	public GameStatus getStatus() {
		return status;
	}
	
	
	private void setStatus(GameStatus newStatus) {
		if (status != newStatus) {
			if (status == GameStatus.WaitingForPlayers && newStatus == GameStatus.Started) {
				gameStartTime = System.currentTimeMillis();
			}
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
