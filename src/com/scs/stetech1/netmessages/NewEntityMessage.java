package com.scs.stetech1.netmessages;

import java.util.HashMap;

import com.jme3.network.serializing.Serializable;
import com.scs.stetech1.components.IEntity;

@Serializable
public class NewEntityMessage extends MyAbstractMessage {
	
	public int entityID;
	//public Vector3f pos;
	//public Quaternion dir;
	public int type;
	public HashMap<String, Object> data = new HashMap<>(); 

	public NewEntityMessage() {
		super();
	}
	
	
	public NewEntityMessage(IEntity e) {
		super(true);
		
		type = e.getType();
		entityID = e.getID();
		//pos = e.getWorldTranslation();
		//dir = e.getWorldRotation();
		data = e.getCreationData();
	}

}
