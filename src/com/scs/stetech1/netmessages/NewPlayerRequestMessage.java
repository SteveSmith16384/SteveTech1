package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class NewPlayerRequestMessage extends MyAbstractMessage {
	
	public String name;
	public byte side;

	public NewPlayerRequestMessage() {
		this(null, -1);
	}
	
	public NewPlayerRequestMessage(String _name, int _side) {
		super(true);
		
		name = _name;
		side = (byte)_side;
	}

}
