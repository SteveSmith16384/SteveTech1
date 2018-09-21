package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class NewGameMessage extends MyAbstractMessage {

	public byte side;

	public NewGameMessage() {
		super(true, false);
	}
	
	
	public NewGameMessage(byte _side) {
		super(true, false);
		
		side =_side;
	}

}
