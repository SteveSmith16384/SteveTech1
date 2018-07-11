package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class ShowMessageMessage extends MyAbstractMessage {

	public String msg;
	
	public ShowMessageMessage() {
		super(true, false);
	}

	public ShowMessageMessage(String _msg, boolean scheduled) {
		super(true, scheduled);
		
		this.msg = _msg;
	}

}
