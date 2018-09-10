package com.scs.stevetech1.server;

import com.scs.stevetech1.data.SimplePlayerData;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.RemoteInput;

import ssmith.util.AverageNumberCalculator;

public class ClientData {

	public enum ClientStatus { Connected, InGame, Spectator };

	public Object networkObj;
	public int id;
	public AverageNumberCalculator pingCalc = new AverageNumberCalculator(4);
	public long latestInputTimestamp; // We ignore any messages sent earlier
	public AbstractServerAvatar avatar;
	public RemoteInput remoteInput = new RemoteInput(); // For storing message that are translated into input
	public long serverToClientDiffTime = 0; // Add to current time to get client time
	public SimplePlayerData playerData; // Class is probably extended for extra data
	public ClientStatus clientStatus = ClientStatus.Connected;
	public boolean sentHello = false;

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
	
	
	public byte getSide() {
		return this.playerData.side;
	}
	
}
