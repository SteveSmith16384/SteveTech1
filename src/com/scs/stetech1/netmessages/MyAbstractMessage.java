package com.scs.stetech1.netmessages;

import java.util.concurrent.atomic.AtomicLong;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class MyAbstractMessage extends AbstractMessage {

	private transient static AtomicLong nextID = new AtomicLong();

	public long msgId;
	public long timestamp = System.currentTimeMillis();
	//public boolean requiresAck;
	
	public MyAbstractMessage(boolean _requiresAck, boolean tcp) {
		super();
		
		msgId = nextID.addAndGet(1);
		this.setReliable(tcp);
		
		requiresAck = _requiresAck;
	}

}
