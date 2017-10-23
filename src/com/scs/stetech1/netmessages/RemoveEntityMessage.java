package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class RemoveEntityMessage extends MyAbstractMessage {
	
	public int entityID;

	public RemoveEntityMessage() {
		super(false, false);
	}
	
	public RemoveEntityMessage(int _entityID) {
		super(false, false);
		
		entityID = _entityID;
	}
	
}