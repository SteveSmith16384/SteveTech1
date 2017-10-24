package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class NewPlayerRequestMessage extends MyAbstractMessage {
	
	public String name;

	public NewPlayerRequestMessage() {
		this(null);
	}
	
	public NewPlayerRequestMessage(String _name) {
		super(true);
		
		name = _name;
	}

}
