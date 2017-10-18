package com.scs.stetech1.netmessages;


public class PingMessage extends MyAbstractMessage {
	
	public long sentTime;

	public PingMessage() {
		super(false);

		sentTime = System.nanoTime();
	}

}
