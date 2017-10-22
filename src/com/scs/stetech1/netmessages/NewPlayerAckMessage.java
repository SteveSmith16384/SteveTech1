package com.scs.stetech1.netmessages;

public class NewPlayerAckMessage extends MyAbstractMessage {

	public long avatarEntityID;
	public int playerID;
	
	public NewPlayerAckMessage(int _playerID, long _avatarEntityID) {
		super(true, true);
		
		playerID = _playerID;
		avatarEntityID = _avatarEntityID;
	}

}
