package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class NewPlayerRequestMessage extends MyAbstractMessage {
	
	public String gameCode;
	public String playerName;

	public NewPlayerRequestMessage() {
		this(null, null);
	}
	
	public NewPlayerRequestMessage(String _gameCode, String _playerName) {
		super(true, false);
		
		gameCode = _gameCode;
		playerName = _playerName;
		//side = (byte)_side;
	}

}
