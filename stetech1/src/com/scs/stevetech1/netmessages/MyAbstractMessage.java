package com.scs.stevetech1.netmessages;

import java.util.concurrent.atomic.AtomicLong;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.server.ClientData;

@Serializable
public class MyAbstractMessage extends AbstractMessage {

	private transient static AtomicLong nextMsgID = new AtomicLong();

	public long msgId;
	public long timestamp = System.currentTimeMillis();
	public transient ClientData client; // for the server to keep track of who sent it
	
	public MyAbstractMessage() { // For serialization
		super();
	}
	
	
	public MyAbstractMessage(boolean tcp) {
		super();
		
		msgId = nextMsgID.addAndGet(1);
		this.setReliable(tcp);
		
	}

}
