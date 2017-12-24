package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class PlayerLeftMessage extends MyAbstractMessage {

	public int playerID;

	public PlayerLeftMessage() {
		super(true);
	}
	
	
	public PlayerLeftMessage(int _playerID) {
		super(true);
		
		playerID = _playerID;
	}

}
