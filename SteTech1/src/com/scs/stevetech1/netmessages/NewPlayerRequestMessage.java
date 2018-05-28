package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class NewPlayerRequestMessage extends MyAbstractMessage {
	
	public String gameCode;
	public String playerName;
	public String key;
	
	public NewPlayerRequestMessage() {
		this(null, null, null);
	}
	
	public NewPlayerRequestMessage(String _gameCode, String _playerName, String _key) {
		super(true, false);
		
		gameCode = _gameCode;
		playerName = _playerName;
		key = _key;
	}

}
