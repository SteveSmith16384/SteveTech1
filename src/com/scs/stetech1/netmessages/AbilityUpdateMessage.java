package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;
import com.scs.stetech1.entities.AbstractAvatar;

@Serializable
public class AbilityUpdateMessage extends MyAbstractMessage {

	public int bulletsLeftInMag;
	public float timeUntilShoot = 0;
	public transient AbstractAvatar avatar;
	public int abilityNum; // 0 = main etc...
	
	public AbilityUpdateMessage() {
		super(true);
	}


	public AbilityUpdateMessage(boolean reliable, AbstractAvatar _avatar) {
		super(reliable);
		
		avatar = _avatar;
	}
	
	
}
