package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class WelcomeClientMessage extends MyAbstractMessage {
	
	public WelcomeClientMessage() {
		super(true);
	}
	
}
