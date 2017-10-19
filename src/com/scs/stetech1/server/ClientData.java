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
	public int id;
	public String name;
	public PacketCache packets = new PacketCache();
	public RemoteInput remoteInput = new RemoteInput();// For storing message that are translated into input

	public ClientData(HostedConnection _conn) {
		id = _conn.getId();
		conn = _conn;
	}


	public void sendMessages(Server myServer) {
		// Ssend entity updates to all
		Iterator<MyAbstractMessage> it = this.packets.getMsgs();
		while (it.hasNext()) {
			myServer.broadcast(Filters.equalTo(conn), it.next());
		}
	}

}
