package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class RemoveEntityMessage extends MyAbstractMessage {
	
	public int entityID;
	public long timeToRemove;

	public RemoveEntityMessage() {
		super(false);
	}
	
	public RemoveEntityMessage(int _entityID) {//, long _timeToRemove) {
		super(false);
		
		entityID = _entityID;
		timeToRemove = System.currentTimeMillis();// _timeToRemove;
	}
	
}