package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

/*
 * Sent from server to client to welcome them
 */
@Serializable
public class WelcomeClientMessage extends MyAbstractMessage {
	
	public WelcomeClientMessage() {
		super(true);
	}
	
}
