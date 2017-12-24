package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class UpdateLobbyMessage extends MyAbstractMessage {

	public String gameServerIPAddress;
	
	public UpdateLobbyMessage() {
		super(false);
	}


	public UpdateLobbyMessage(String _gameServerIPAddress) {
		super(false);
		
		gameServerIPAddress = _gameServerIPAddress;
	}

}
