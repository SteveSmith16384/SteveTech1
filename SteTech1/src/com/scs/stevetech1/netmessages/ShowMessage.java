package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class ShowMessage extends MyAbstractMessage {

	public String msg;
	
	public ShowMessage() {
		super(true, false);
	}

	public ShowMessage(String _msg, boolean scheduled) {
		super(true, scheduled);
		
		this.msg = _msg;
	}

}
