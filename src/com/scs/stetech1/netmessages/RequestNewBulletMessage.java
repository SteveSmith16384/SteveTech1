package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class RequestNewBulletMessage extends MyAbstractMessage {

	public int type;
	
	public RequestNewBulletMessage() {
		super(true);
	}
	

	public RequestNewBulletMessage(int _type) {
		this();
		
		type = _type;
	}

}
