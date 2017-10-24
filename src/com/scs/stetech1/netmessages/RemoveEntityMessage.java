package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class RemoveEntityMessage extends MyAbstractMessage {
	
	public int entityID;

	public RemoveEntityMessage() {
		super(false);
	}
	
	public RemoveEntityMessage(int _entityID) {
		super(false);
		
		entityID = _entityID;
	}
	
}