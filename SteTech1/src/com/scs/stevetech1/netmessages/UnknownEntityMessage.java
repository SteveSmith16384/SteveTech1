package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class UnknownEntityMessage extends MyAbstractMessage {

	public int entityID;

	public UnknownEntityMessage() {
		super();
	}

	
	public UnknownEntityMessage(int _entityID) {
		super(true, false);
		
		entityID = _entityID;
	}

}
