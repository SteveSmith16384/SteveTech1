package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class RemoveEntityMessage extends MyAbstractMessage {
	
	public int entityID;

	public RemoveEntityMessage() {
		super();
	}
	
	
	public RemoveEntityMessage(int _entityID) {
		super(true, true);
		
		entityID = _entityID;
	}
	
}