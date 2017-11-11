package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class JoinGameFailedMessage extends MyAbstractMessage {

	public JoinGameFailedMessage() {
		super(true);
		// todo - reason why
	}

}
