package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class PingMessage extends MyAbstractMessage {
	
	public long serverSentTime;
	public long clientSentTime;

	public PingMessage() {
		super(false, false);

		serverSentTime = System.currentTimeMillis();
	}

}
