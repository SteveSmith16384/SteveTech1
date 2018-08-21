package com.scs.stevetech1.netmessages.connecting;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.netmessages.MyAbstractMessage;

/*
 * Sent from server to client to welcome them
 */
@Serializable
public class WelcomeClientMessage extends MyAbstractMessage {
	
	public WelcomeClientMessage() {
		super(true, false);
	}
	
}
