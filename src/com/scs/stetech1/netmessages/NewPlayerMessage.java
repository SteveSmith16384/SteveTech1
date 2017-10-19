package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class NewPlayerMessage extends MyAbstractMessage {
	
	public String name;

	public NewPlayerMessage() {
		this(null);
	}
	
	public NewPlayerMessage(String _name) {
		super(true);
		
		name = _name;
	}

}
