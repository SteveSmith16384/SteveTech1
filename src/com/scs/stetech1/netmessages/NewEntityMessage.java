package com.scs.stetech1.netmessages;

import java.util.HashMap;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.scs.stetech1.entities.PhysicalEntity;

@Serializable
public class NewEntityMessage extends MyAbstractMessage {
	
	public int entityID;
	public Vector3f pos;
	public Quaternion dir;
	//public boolean force; // Force new position on client, e.g. avatar restarting.
	public int type;
	public HashMap<String, Object> data = new HashMap<>(); 

	public NewEntityMessage() {
		super();
	}
	
	
	public NewEntityMessage(PhysicalEntity e) {
		super(true);
		
		type = e.getType();
		entityID = e.getID();
		pos = e.getWorldTranslation();
		dir = e.getWorldRotation();
		data = e.getCreationData();
	}

}
