package com.scs.stetech1.netmessages;

import com.jme3.network.AbstractMessage;

public class AckMessage extends AbstractMessage {

	public int ackingId;
	
	public AckMessage(int _ackingId) {
		ackingId = _ackingId;
	}

}
