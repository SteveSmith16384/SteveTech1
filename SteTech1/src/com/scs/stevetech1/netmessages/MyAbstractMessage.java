package com.scs.stevetech1.netmessages;

import java.util.concurrent.atomic.AtomicLong;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.server.ClientData;

@Serializable
public class MyAbstractMessage { //extends AbstractMessage {

	private transient static AtomicLong nextMsgID = new AtomicLong();

	private boolean reliable = true; // Use TCP if true
	public boolean scheduled = false; // If true, the client should not process it until the client render time reaches the msg timestamp
	
	public long msgId;
	public long timestamp = System.currentTimeMillis();
	public transient ClientData client; // for the server to keep track of who sent it
	
	public MyAbstractMessage() { // For serialization
		super();
	}
	
	
	public MyAbstractMessage(boolean tcp, boolean _scheduled) {
		super();
		
		msgId = nextMsgID.addAndGet(1);
		this.setReliable(tcp);
		this.scheduled = _scheduled;
		
	}

	
	protected void setReliable(boolean b) {
		this.reliable = b;
	}

	
	public boolean isReliable() {
		return this.reliable;
	}
}