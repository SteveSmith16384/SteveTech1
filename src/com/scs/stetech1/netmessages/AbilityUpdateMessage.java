package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class AbilityUpdateMessage extends MyAbstractMessage { // todo - send these from server to client

	public int bulletsLeftInMag;
	public float timeUntilShoot = 0;

	public AbilityUpdateMessage() {
		super(false);
	}


	public AbilityUpdateMessage(boolean reliable) {
		super(reliable);
	}
	
	
}
