package com.scs.stevetech1.netmessages.connecting;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.netmessages.MyAbstractMessage;

@Serializable
public class GameSuccessfullyJoinedMessage extends MyAbstractMessage {

	public byte side;
	public int playerID;

	public GameSuccessfullyJoinedMessage() {
		super(true, false);
	}
	
	
	public GameSuccessfullyJoinedMessage(int _playerID, byte _side) {
		super(true, false);
		
		playerID = _playerID;
		side =_side;
	}

}
