package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class NewPlayerAckMessage extends MyAbstractMessage {

	public long avatarEntityID;
	public int playerID;

	public NewPlayerAckMessage() {
		super(true);
	}
	
	
	public NewPlayerAckMessage(int _playerID, long _avatarEntityID) {
		super(true);
		
		playerID = _playerID;
		avatarEntityID = _avatarEntityID;
	}

}
