package com.scs.stetech1.data;

import java.util.ArrayList;

import com.scs.stetech1.entities.AbstractPlayersAvatar;

public class GameData {

	public enum Status {WaitingForPlayers, Started }

	public Status status;
	public String name;
	public ArrayList[] players = new ArrayList[2]; // By side // <AbstractPlayersAvatar> 
	
	
	public GameData() {
		players[0] = new ArrayList();
		players[1] = new ArrayList();
	}


	public byte getSide() {
		if (players[0].size() <= players[1].size()) {
			return 0;
		} else {
			return 1;
		}
	}
	
	
	public void addPlayer(AbstractPlayersAvatar avatar) {
		players[avatar.side].add(avatar);
	}
	

	public void removePlayer(AbstractPlayersAvatar avatar) {// todo - remove players from lists when they leave
		players[avatar.side].remove(avatar);
		// todo - check there are still ernough players
	}
	
}
