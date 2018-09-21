package com.scs.stevetech1.netmessages.connecting;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.netmessages.MyAbstractMessage;

@Serializable
public class ServerSuccessfullyJoinedMessage extends MyAbstractMessage {

	public int playerID;

	public ServerSuccessfullyJoinedMessage() {
		super(true, false);
	}
	
	
	public ServerSuccessfullyJoinedMessage(int _playerID) {
		super(true, false);
		
		playerID = _playerID;
	}

}
