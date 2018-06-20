package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class GenericStringMessage extends MyAbstractMessage { // todo - remove this (replaced by gamelog)

	public String msg;
	
	public GenericStringMessage() {
		super(true, false);
	}

	public GenericStringMessage(String _msg, boolean scheduled) {
		super(true, scheduled);
		
		this.msg = _msg;
	}

}
