package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class PingMessage extends MyAbstractMessage {
	
	public boolean s2c; // Is it server2client or vice-versa
	public long originalSentTime;
	public long responseSentTime;
	public int randomCode;

	public PingMessage() {
		super();
	}
	
	
	public PingMessage(boolean _s2c, int _randomCode) {
		super(true, false);

		s2c = _s2c;
		originalSentTime = System.currentTimeMillis();
		randomCode = _randomCode;
	}

}
