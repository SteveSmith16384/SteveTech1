package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

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
