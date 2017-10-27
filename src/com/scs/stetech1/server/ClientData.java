package com.scs.stetech1.server;

import com.jme3.network.HostedConnection;
import com.scs.stetech1.input.RemoteInput;
import com.scs.stetech1.shared.AveragePingTime;

public class ClientData {

	public HostedConnection conn;
	public AveragePingTime pingCalc = new AveragePingTime();
	public long pingRTT;
	public String playerName;
	public long latestInputTimestamp;
	public int avatarID;
	public RemoteInput remoteInput = new RemoteInput();// For storing message that are translated into input
	//public boolean isInGame = false;
	public long serverToClientDiffTime = 0; // Add to current time to get client time
	
	public ClientData(HostedConnection _conn) {
		conn = _conn;
	}


	public long getClientTime() {
		return System.currentTimeMillis() + serverToClientDiffTime;
	}

	
	public int getPlayerID() {
		return conn.getId();
	}

}
