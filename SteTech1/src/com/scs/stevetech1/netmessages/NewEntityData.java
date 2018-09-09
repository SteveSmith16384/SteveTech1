package com.scs.stevetech1.netmessages;

import java.util.HashMap;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.components.IEntity;

@Serializable
public class NewEntityData {

	public int entityID;
	public byte type; // Entity code
	public HashMap<String, Object> data = new HashMap<>(); // todo - use codes instead of string
	public boolean clientShouldAddImmed = false; // e.g. for players bullets, create them immediately - -todo - remove this and add imed if playerid > 0
	
	public NewEntityData() {
		
	}

	public NewEntityData(IEntity e) {
		this.entityID = e.getID();
		this.type = (byte) e.getType(); // todo - check > 127
		this.data = e.getCreationData();
	}

}
