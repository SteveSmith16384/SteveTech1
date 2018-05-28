package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

/*
 * Sent from a client to the game server to indicate they are leaving
 */
@Serializable
public class PlayerLeftMessage extends MyAbstractMessage {

	public int playerID;

	public PlayerLeftMessage() {
		super();
	}
	
	
	public PlayerLeftMessage(int _playerID) {
		super(true, false);
		
		playerID = _playerID;
	}

}
