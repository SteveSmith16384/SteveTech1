package com.scs.stetech1.server;

import com.jme3.network.HostedConnection;
import com.scs.stetech1.input.RemoteInput;

public class ClientData {

	public HostedConnection conn;
	public long ping;
	public String name;
	public long latestInputTimestamp;
	public int avatarID;
	//public PacketCache packets = new PacketCache();
	public RemoteInput remoteInput = new RemoteInput();// For storing message that are translated into input
	//public boolean isInGame = false;
	public long clientDiffTime = 0;
	
	public ClientData(HostedConnection _conn) {
		conn = _conn;
	}


	/*public int getMsgID() {
	}*/
	
	
	/*public void sendMessages(Server myServer) {
	}*/
	
	public long getClientTime() {
		return System.currentTimeMillis() + clientDiffTime;
	}
	
	public int getPlayerID() {
		return conn.getId();
	}

}
