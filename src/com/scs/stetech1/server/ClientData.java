package com.scs.stetech1.server;

import com.jme3.network.HostedConnection;

public class ClientData {

	public HostedConnection conn;
	public long ping;
	public int id;
	
	public ClientData(int _id, HostedConnection _conn) {
		id = _id;
		conn = _conn;
	}


}
