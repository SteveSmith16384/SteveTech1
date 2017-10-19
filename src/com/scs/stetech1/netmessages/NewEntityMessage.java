package com.scs.stetech1.netmessages;

import java.util.HashMap;

import com.jme3.network.serializing.Serializable;
import com.scs.stetech1.components.ISharedEntity;

@Serializable
public class NewEntityMessage extends EntityUpdateMessage {
	
	public int type, entityID;
	public HashMap<String, Object> data = new HashMap<>(); 

	public NewEntityMessage() {
		super();
	}
	
	
	public NewEntityMessage(ISharedEntity e) {
		super(e);
		
		this.requiresAck = true;
		
		entityID = e.getID();
		type = e.getType();
		data = e.getCreationData();
	}

}
