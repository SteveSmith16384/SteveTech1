package com.scs.stetech1.netmessages;

import java.util.HashMap;

import com.jme3.network.serializing.Serializable;
import com.scs.stetech1.shared.entities.PhysicalEntity;

@Serializable
public class NewEntityMessage extends EntityUpdateMessage {
	
	//public int entityID;  This is in the superclass!
	public int type;
	public HashMap<String, Object> data = new HashMap<>(); 

	public NewEntityMessage() {
		super();
	}
	
	
	public NewEntityMessage(PhysicalEntity e) {
		super(e, true);
		
		this.setReliable(true); // Since superclass has set it to false
		
		type = e.getType();
		data = e.getCreationData();
	}

}
