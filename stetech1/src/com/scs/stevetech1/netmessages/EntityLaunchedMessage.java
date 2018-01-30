package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class EntityLaunchedMessage extends MyAbstractMessage {

	public int entityID;

	public EntityLaunchedMessage() {
		super(true);
	}


	public EntityLaunchedMessage(int eid) {
		this();
		
		entityID = eid;
	}
	
}
