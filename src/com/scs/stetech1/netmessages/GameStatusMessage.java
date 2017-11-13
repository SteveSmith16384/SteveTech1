package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class GameStatusMessage extends MyAbstractMessage {

	public GameStatusMessage() {
		super(true);
	}

}
