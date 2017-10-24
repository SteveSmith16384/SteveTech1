package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class PingMessage extends MyAbstractMessage {
	
	public boolean s2c;
	public long originalSentTime;
	public long responseSentTime;

	public PingMessage() {
		super(false);
	}
	
	
	public PingMessage(boolean _s2c) {
		super(false);

		s2c = _s2c;
		originalSentTime = System.currentTimeMillis();
	}

}
