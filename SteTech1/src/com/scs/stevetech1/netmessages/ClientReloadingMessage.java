package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class ClientReloadingMessage extends MyAbstractMessage {
	
	public int abilityId;
	
	public ClientReloadingMessage() {
		super();
	}


	public ClientReloadingMessage(int _abilityId) {
		super(true, false);
		
		abilityId = _abilityId;
	}

}
