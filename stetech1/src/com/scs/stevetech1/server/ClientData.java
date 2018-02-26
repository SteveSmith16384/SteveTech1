package com.scs.stevetech1.server;

import com.scs.stevetech1.data.SimplePlayerData;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.RemoteInput;
import com.scs.stevetech1.shared.AverageNumberCalculator;

public class ClientData {

	public enum ClientStatus { Connected, Accepted, Spectating }; // Accepted == has been given a slot in the game

	public Object networkObj;
	public int id;
	public AverageNumberCalculator pingCalc = new AverageNumberCalculator();
	public long latestInputTimestamp;
	public AbstractServerAvatar avatar;
	public RemoteInput remoteInput = new RemoteInput(); // For storing message that are translated into input
	public long serverToClientDiffTime = 0; // Add to current time to get client time
	public SimplePlayerData playerData;
	public ClientStatus clientStatus = ClientStatus.Connected;

	private int score;
	public int side = -1;

	public ClientData(int _id, Object _networkObj) {
		super();
		
		id = _id;
		networkObj = _networkObj;
	}


	public long getClientTime() {
		return System.currentTimeMillis() + serverToClientDiffTime;
	}


	public int getPlayerID() {
		return id;
	}
	
	
	public int getScore() {
		return this.score;
	}


	public void incScore(int i) {
		this.score += i;
	}


	public void setScore(int i) {
		this.score = i;
	}

}
