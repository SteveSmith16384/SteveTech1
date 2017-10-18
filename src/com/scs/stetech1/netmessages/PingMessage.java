package com.scs.stetech1.netmessages;

import com.jme3.network.AbstractMessage;

public class PingMessage extends AbstractMessage {
	
	public long sentTime;

	public PingMessage() {
		sentTime = System.nanoTime();
	}

}
