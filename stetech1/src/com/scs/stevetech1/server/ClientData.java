package com.scs.stevetech1.server;

import com.jme3.input.InputManager;
import com.jme3.renderer.Camera;
import com.scs.stevetech1.data.SimplePlayerData;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.RemoteInput;
import com.scs.stevetech1.shared.AverageNumberCalculator;

public class ClientData {

	public enum ClientStatus { Connected, Accepted }; // Accepted == has avatar and is in-game

	public Object networkObj;
	public int id;
	public AverageNumberCalculator pingCalc = new AverageNumberCalculator();
	public long latestInputTimestamp;
	public AbstractServerAvatar avatar;
	public RemoteInput remoteInput = new RemoteInput(); // For storing message that are translated into input
	public long serverToClientDiffTime = 0; // Add to current time to get client time
	public SimplePlayerData playerData;
	public ClientStatus clientStatus = ClientStatus.Connected;

	public ClientData(int _id, Object _networkObj, Camera cam, InputManager _inputManager) {
		id = _id;
		networkObj = _networkObj;
	}


	public long getClientTime() {
		return System.currentTimeMillis() + serverToClientDiffTime;
	}


	public int getPlayerID() {
		return id;
	}

}
