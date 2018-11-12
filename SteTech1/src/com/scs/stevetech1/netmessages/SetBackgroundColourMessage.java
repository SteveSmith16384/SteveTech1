package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class SetBackgroundColourMessage extends MyAbstractMessage {
	
	public float a, r, g, b;
	
	public SetBackgroundColourMessage() {
		// Kryo
	}
	
	
	public SetBackgroundColourMessage(float _a, float _r, float _g, float _b) {
		super(true, false);
		
		a =_a;
		r = _r;
		g = _g;
		b = _b;
		
	}

}
