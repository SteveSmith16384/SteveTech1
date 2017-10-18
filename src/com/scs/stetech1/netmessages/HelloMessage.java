package com.scs.stetech1.netmessages;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class HelloMessage extends AbstractMessage {

	private String hello;       // custom message data

	public HelloMessage() {

	}    // empty constructor

	public HelloMessage(String s) { 
		hello = s; 
	} // custom constructor

	public String getMessage() {
		return hello;
	}
	
}