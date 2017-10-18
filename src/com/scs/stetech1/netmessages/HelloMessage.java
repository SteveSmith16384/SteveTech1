package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class HelloMessage extends MyAbstractMessage {

	private String hello;

	public HelloMessage() {
		super(false);

	}

	
	public HelloMessage(String s) {
		this();
		
		hello = s; 
	}

	
	public String getMessage() {
		return hello;
	}
	
}