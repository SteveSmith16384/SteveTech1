package com.scs.stevetech1.netmessages;

import java.util.HashMap;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.components.IEntity;

@Serializable
public class NewEntityData {

	public int entityID;
	public int type; // Entity code
	public HashMap<String, Object> data = new HashMap<>(); // todo - use codes instead of string
	
	public NewEntityData() {
		
	}

	public NewEntityData(IEntity e) {
		this.entityID = e.getID();
		this.type = e.getType();
		this.data = e.getCreationData();
	}

}
