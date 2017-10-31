package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class AllEntitiesSentMessage extends MyAbstractMessage {

	public AllEntitiesSentMessage() {
		super(true);
	}


}