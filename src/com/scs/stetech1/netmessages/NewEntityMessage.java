package com.scs.stetech1.netmessages;

import com.scs.stetech1.components.ISharedEntity;

public class NewEntityMessage extends EntityUpdateMessage {
	
	public int type, id;

	public NewEntityMessage(ISharedEntity e) {
		super(e);
		
		this.requiresAck = true;
		
		id = e.getID();
		type = e.getType();
	}

}
