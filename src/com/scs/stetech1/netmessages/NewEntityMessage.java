package com.scs.stetech1.netmessages;

import java.util.HashMap;

import com.scs.stetech1.components.ISharedEntity;

public class NewEntityMessage extends EntityUpdateMessage {
	
	public int type, entityID;
	public HashMap<String, Object> data = new HashMap<>(); 

	public NewEntityMessage(ISharedEntity e) {
		super(e);
		
		this.requiresAck = true;
		
		entityID = e.getID();
		type = e.getType();
		data = e.getCreationData();
	}

}
