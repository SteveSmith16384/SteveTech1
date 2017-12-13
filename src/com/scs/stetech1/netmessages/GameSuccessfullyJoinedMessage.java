package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class GameSuccessfullyJoinedMessage extends MyAbstractMessage {

	public int side;
	public int playerID;

	public GameSuccessfullyJoinedMessage() {
		super(true);
	}
	
	
	public GameSuccessfullyJoinedMessage(int _playerID, int _side) {
		super(true);
		
		playerID = _playerID;
		side =_side;
	}

}
