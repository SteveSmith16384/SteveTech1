package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class NumEntitiesMessage extends MyAbstractMessage {
	
	public int num;
	
	public NumEntitiesMessage() {
		this(0);
	}
	
	public NumEntitiesMessage(int _num) {
		super(true, false);

		num = _num;
	}

}
