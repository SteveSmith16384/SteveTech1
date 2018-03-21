package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class NewPlayerRequestMessage extends MyAbstractMessage {
	
	public String gameID;
	public String playerName;

	public NewPlayerRequestMessage() {
		this(null, null);
	}
	
	public NewPlayerRequestMessage(String _gameID, String _playerName) {
		super(true, false);
		
		gameID = _gameID;
		playerName = _playerName;
		//side = (byte)_side;
	}

}
