package com.scs.stevetech1.netmessages.connecting;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.netmessages.MyAbstractMessage;

/**
 * Sent from server to client, so the client knows the server has responded.
 * 
 * @author stephencs
 *
 */
@Serializable
public class HelloMessage extends MyAbstractMessage {
	
	public HelloMessage() {
		
	}
	
}
