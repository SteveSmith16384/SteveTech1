package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class RequestNewBulletMessage extends MyAbstractMessage {

	public int ownerEntityID = -1;
	public int type = -1;
	
	public RequestNewBulletMessage() {
		super(true);
	}
	

	public RequestNewBulletMessage(int _type, int _entityID) {
		this();
		
		type = _type;
		ownerEntityID = _entityID;
	}

}
