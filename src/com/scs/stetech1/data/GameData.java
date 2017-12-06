package com.scs.stetech1.data;

import java.util.ArrayList;

import com.scs.stetech1.entities.AbstractAvatar;
import com.scs.stetech1.server.ClientData;

public class GameData {

	public enum Status {WaitingForPlayers, Started, Finished }

	private int maxPlayersPerSide;
	private int maxSides;
	private Status status;
	public String name;
	private long gameStartTime;
	public ArrayList<ClientData> players = new ArrayList<ClientData>();
	
	public GameData(int _maxPlayersPerSide, int _maxSides) {
		super();
		
		maxPlayersPerSide = _maxPlayersPerSide;
		maxSides = _maxSides;
	}

	
	public void checkGameStatus() {
		// todo
	}
	
	
	public void setName(String n) {			
		name = n;
	}


	public Status getStatus() {
		return status;
	}
	
	
	public void setStatus(Status newStatus) {
		if (status != newStatus) {
			if (status == Status.WaitingForPlayers && newStatus == Status.Started) {
				gameStartTime = System.currentTimeMillis();
			}
		}
	}
	
	
	public void addPlayer(ClientData avatar) {
		players.add(avatar);
	}
	

	public void removePlayer(ClientData avatar) {
		players.remove(avatar);
	}
	
}
