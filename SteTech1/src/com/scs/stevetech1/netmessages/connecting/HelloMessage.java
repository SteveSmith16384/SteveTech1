package com.scs.stevetech1.netmessages.connecting;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.netmessages.MyAbstractMessage;

/**
 * Sent from server to client, in case we need to send something before the client can
 * send to the server.
 * 
 * @author stephencs
 *
 */
@Serializable
public class HelloMessage extends MyAbstractMessage {
	
	public HelloMessage() {
		
	}
	
}
