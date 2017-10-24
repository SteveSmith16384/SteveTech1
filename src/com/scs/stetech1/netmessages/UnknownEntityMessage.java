package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class UnknownEntityMessage extends MyAbstractMessage {

	public int entityID;

	public UnknownEntityMessage() {
		super(true);
	}

	
	public UnknownEntityMessage(int _entityID) {
		super(true);
		
		entityID = _entityID;
	}

}
