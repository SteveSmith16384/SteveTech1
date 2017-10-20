package com.scs.stetech1.server;

import java.util.Iterator;

import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Server;
import com.scs.stetech1.input.RemoteInput;
import com.scs.stetech1.netmessages.MyAbstractMessage;
import com.scs.stetech1.shared.PacketCache;

public class ClientData {

	public HostedConnection conn;
	public long ping;
	//public int id;
	public String name;
	//public PacketCache packets = new PacketCache();
	public RemoteInput remoteInput = new RemoteInput();// For storing message that are translated into input
	//public boolean isInGame = false;
	
	public ClientData(HostedConnection _conn) {
		conn = _conn;
	}


	public void sendMessages(Server myServer) {
	}
	
	
	public int getPlayerID() {
		return conn.getId();
	}

}
