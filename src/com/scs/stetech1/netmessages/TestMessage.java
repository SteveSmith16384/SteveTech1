package com.scs.stetech1.netmessages;

public class TestMessage extends MyAbstractMessage {

	public int num;
	public byte[] b = new byte[1000];
	
	public TestMessage() {
		this.setReliable(false);
	}

	public TestMessage(int i) {
		this();
		
		num = i;
	}

}
