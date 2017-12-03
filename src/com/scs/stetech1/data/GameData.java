package com.scs.stetech1.data;

import java.util.ArrayList;

import com.scs.stetech1.entities.AbstractPlayersAvatar;

public class GameData {

	public enum Status {WaitingForPlayers, Started }

	private Status status;
	public String name;
	private long gameStartTime;
	public ArrayList<AbstractPlayersAvatar> players = new ArrayList<AbstractPlayersAvatar>();
	
	
	public GameData() {
	}
				
	public void setName(String n) {			
		name = n;
	}

/*
	public byte getSide() {
		if (players[0].size() <= players[1].size()) {
			return 0;
		} else {
			return 1;
		}
	}
	*/
	
	public Status getStatus() {
		return status;
	}
	
	
	public void setStatus(Status s) {
		if (status != s) {
			if (status == Status.WaitingForPlayers && s == Status.Started) {
				gameStartTime = System.currentTimeMillis();
			}
		}
	}
	
	
	public void addPlayer(AbstractPlayersAvatar avatar) {
		players.add(avatar);
	}
	

	public void removePlayer(AbstractPlayersAvatar avatar) {
		players.remove(avatar);
	}
	
}
