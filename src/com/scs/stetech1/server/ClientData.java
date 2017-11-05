package com.scs.stetech1.server;

import com.jme3.network.HostedConnection;
import com.scs.stetech1.input.RemoteInput;
import com.scs.stetech1.shared.AverageNumberCalculator;

public class ClientData {

	public HostedConnection conn;
	public AverageNumberCalculator pingCalc = new AverageNumberCalculator();
	public long pingRTT;
	public String playerName;
	public long latestInputTimestamp;
	public int avatarID;
	public RemoteInput remoteInput = new RemoteInput();// For storing message that are translated into input
	public long serverToClientDiffTime = 0; // Add to current time to get client time
	public byte side;
	
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
