package com.scs.stevetech1.netmessages;

import java.util.HashMap;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.components.IEntity;

@Serializable
public class NewEntityMessage extends MyAbstractMessage {
	
	public int entityID;
	public int type;
	public HashMap<String, Object> data = new HashMap<>(); 

	public NewEntityMessage() {
		super();
	}
	
	
	public NewEntityMessage(IEntity e) {
		super(true, true);
		
		type = e.getType();
		entityID = e.getID();
		data = e.getCreationData();
	}

}
