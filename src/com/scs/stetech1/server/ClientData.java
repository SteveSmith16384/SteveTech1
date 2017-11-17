package com.scs.stetech1.server;

import com.jme3.input.InputManager;
import com.jme3.network.HostedConnection;
import com.jme3.renderer.Camera;
import com.scs.stetech1.input.RemoteInput;
import com.scs.stetech1.server.entities.ServerPlayersAvatar;
import com.scs.stetech1.shared.AverageNumberCalculator;

public class ClientData {

	public enum Status { Connected, InGame };

	public HostedConnection conn;
	public AverageNumberCalculator pingCalc = new AverageNumberCalculator();
	public long pingRTT;
	public String playerName;
	public long latestInputTimestamp;
	public ServerPlayersAvatar avatar;
	public RemoteInput remoteInput = new RemoteInput();// For storing message that are translated into input
	public long serverToClientDiffTime = 0; // Add to current time to get client time
	public byte side;
	public Status clientStatus = Status.Connected;

	public ClientData(HostedConnection _conn, Camera cam, InputManager _inputManager) {
		conn = _conn;
	}


	public long getClientTime() {
		return System.currentTimeMillis() + serverToClientDiffTime;
	}


	public int getPlayerID() {
		return conn.getId();
	}

}
