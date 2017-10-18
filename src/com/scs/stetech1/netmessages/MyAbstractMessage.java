package com.scs.stetech1.netmessages;

import java.util.concurrent.atomic.AtomicInteger;

import com.jme3.network.AbstractMessage;

public class MyAbstractMessage extends AbstractMessage {

	public int id;
	private static AtomicInteger nextID = new AtomicInteger();
	public long timestamp = System.nanoTime();
	public boolean requiresAck;
	
	public MyAbstractMessage(boolean _requiresAck) {
		super();
		
		id = nextID.addAndGet(1);
		this.setReliable(false); // UDP
		
		requiresAck = _requiresAck;
	}

}
