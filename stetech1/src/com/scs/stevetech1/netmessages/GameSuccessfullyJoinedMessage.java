package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class GameSuccessfullyJoinedMessage extends MyAbstractMessage {

	public int side;
	public int playerID;

	public GameSuccessfullyJoinedMessage() {
		super();
	}
	
	
	public GameSuccessfullyJoinedMessage(int _playerID, int _side) {
		super(true, false);
		
		playerID = _playerID;
		side =_side;
	}

}
