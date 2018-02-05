package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class DebuggingMessage extends MyAbstractMessage {

	public DebuggingMessage() {
		super(true, false);
	}
	
}
