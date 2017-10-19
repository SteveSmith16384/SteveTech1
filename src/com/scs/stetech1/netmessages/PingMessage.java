package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class PingMessage extends MyAbstractMessage {
	
	public long sentTime;

	public PingMessage() {
		super(false);

		sentTime = System.currentTimeMillis();
	}

}
