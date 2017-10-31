package com.scs.stetech1.netmessages;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.scs.stetech1.shared.entities.PhysicalEntity;

@Serializable
public class EntityUpdateMessage extends MyAbstractMessage {
	
	public int entityID;
	public Vector3f pos;
	public Quaternion dir;
	public boolean force; // Force new position on client, e.g. avatar restarting.

	public EntityUpdateMessage() {
		super(false);
	}
	
	public EntityUpdateMessage(PhysicalEntity e) {
		super(false);
		
		entityID = e.getID();
		pos = e.getWorldTranslation();
		dir = e.getWorldRotation();
	}

}
