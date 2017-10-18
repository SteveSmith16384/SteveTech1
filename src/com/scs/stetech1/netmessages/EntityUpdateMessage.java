package com.scs.stetech1.netmessages;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.scs.stetech1.components.ISharedEntity;

public class EntityUpdateMessage extends MyAbstractMessage {
	
	public int id;
	public Vector3f pos;
	public Quaternion dir;
	public boolean force; // Force new position on client, e.g. avatar restarting
	
	public EntityUpdateMessage(ISharedEntity e) {
		super(false);
		
		id = e.getID();
		pos = e.getLocalTranslation();
		dir = e.getRotation();
	}

}
