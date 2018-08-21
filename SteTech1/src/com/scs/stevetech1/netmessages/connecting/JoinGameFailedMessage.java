package com.scs.stevetech1.netmessages.connecting;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.netmessages.MyAbstractMessage;

@Serializable
public class JoinGameFailedMessage extends MyAbstractMessage {

	public String reason;
	
	public JoinGameFailedMessage() {
		super(true, false);
	}

	public JoinGameFailedMessage(String _reason) {
		this();
		
		this.reason = _reason;
	}

}
