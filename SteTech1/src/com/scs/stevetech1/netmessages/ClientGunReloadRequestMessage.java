package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

/**
 * The client wants to reload the weapon
 *
 */
@Serializable
public class ClientGunReloadRequestMessage extends MyAbstractMessage {
	
	public int abilityId;
	
	public ClientGunReloadRequestMessage() {
		super();
	}


	public ClientGunReloadRequestMessage(int _abilityId) {
		super(true, false);
		
		abilityId = _abilityId;
	}

}
