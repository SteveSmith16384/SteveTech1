package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class NewClientOnlyEntity extends MyAbstractMessage {
	
	//public int type;
	//public Vector3f pos;
	public NewEntityData data;
	
	public NewClientOnlyEntity() {
		super(true, true);
	}
	
	
	public NewClientOnlyEntity(NewEntityData _data) {
		this();
		
		//type = _type;
		//pos = _pos;
		data = _data;
	}

}
