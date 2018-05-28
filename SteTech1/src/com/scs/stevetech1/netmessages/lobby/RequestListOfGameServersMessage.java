package com.scs.stevetech1.netmessages.lobby;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.netmessages.MyAbstractMessage;

@Serializable
public class RequestListOfGameServersMessage extends MyAbstractMessage {

	public RequestListOfGameServersMessage() {
		super(true, false);
	}

}
