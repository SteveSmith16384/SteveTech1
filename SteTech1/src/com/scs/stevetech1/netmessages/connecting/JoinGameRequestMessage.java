package com.scs.stevetech1.netmessages.connecting;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.netmessages.MyAbstractMessage;

@Serializable
public class JoinGameRequestMessage extends MyAbstractMessage {
	
	public String gameCode;
	public String playerName;
	public String key;
	public double clientVersion;

	public JoinGameRequestMessage() {
		this(null, -1, null, null);
	}
	
	
	public JoinGameRequestMessage(String _gameCode, double _clientVersion, String _playerName, String _key) {
		super(true, false);
		
		gameCode = _gameCode;
		clientVersion = _clientVersion;
		playerName = _playerName;
		key = _key;
	}

}
