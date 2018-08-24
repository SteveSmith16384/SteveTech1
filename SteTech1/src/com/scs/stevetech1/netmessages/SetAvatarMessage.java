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
		super(true, false); // Must not be scheduled, since we're about to send all the entities to the client, and they need to know which one is theirs
		
		playerID = _playerID;
		avatarEntityID = _avatarEntityID;
	}

}
