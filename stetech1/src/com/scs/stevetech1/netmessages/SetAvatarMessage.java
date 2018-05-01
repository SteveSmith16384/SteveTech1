package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class SetAvatarMessage extends MyAbstractMessage {
	
	public int playerID;
	public int avatarEntityID;
	
	public SetAvatarMessage() {
		// Kryo
	}
	
	
	public SetAvatarMessage(int _playerID, int _avatarEntityID) {
		super(true, true);
		
		playerID = _playerID;
		avatarEntityID = _avatarEntityID;
	}

}
