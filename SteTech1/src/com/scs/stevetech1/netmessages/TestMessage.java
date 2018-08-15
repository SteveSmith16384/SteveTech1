package com.scs.stevetech1.netmessages;

public class TestMessage extends MyAbstractMessage {

	public int num;
	public byte[] b = new byte[1000];
	
	public TestMessage() {
		this.setUseTCP(false);
	}

	public TestMessage(int i) {
		this();
		
		num = i;
	}

}
