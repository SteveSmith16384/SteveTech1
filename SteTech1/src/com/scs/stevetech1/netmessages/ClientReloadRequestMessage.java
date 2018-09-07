package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

/**
 * The client wants to reload the weapon
 *
 */
@Serializable
public class ClientReloadRequestMessage extends MyAbstractMessage {
	
	public int abilityId;
	
	public ClientReloadRequestMessage() {
		super();
	}


	public ClientReloadRequestMessage(int _abilityId) {
		super(true, false);
		
		abilityId = _abilityId;
	}

}
