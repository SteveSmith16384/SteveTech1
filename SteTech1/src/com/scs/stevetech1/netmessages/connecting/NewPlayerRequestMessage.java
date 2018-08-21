package com.scs.stevetech1.netmessages.connecting;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.netmessages.MyAbstractMessage;

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
