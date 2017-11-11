package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class GameSuccessfullyJoinedMessage extends MyAbstractMessage {

	public long avatarEntityID;
	public int playerID;

	public GameSuccessfullyJoinedMessage() {
		super(true);
	}
	
	
	public GameSuccessfullyJoinedMessage(int _playerID, long _avatarEntityID) {
		super(true);
		
		playerID = _playerID;
		avatarEntityID = _avatarEntityID;
	}

}
