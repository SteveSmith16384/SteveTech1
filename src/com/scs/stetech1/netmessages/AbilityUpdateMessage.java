package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;
import com.scs.stetech1.entities.AbstractAvatar;
import com.scs.stetech1.shared.IAbility;

@Serializable
public class AbilityUpdateMessage extends MyAbstractMessage {

	public int bulletsLeftInMag;
	public float timeUntilShoot = 0;
	//public transient AbstractAvatar avatar;
	public int avatarId;
	public int abilityNum; // 0 = main etc...
	
	public AbilityUpdateMessage() {
		super(true);
	}


	public AbilityUpdateMessage(boolean reliable, AbstractAvatar avatar, int num) {
		super(reliable);
		
		avatarId = avatar.id;
		abilityNum = num;
		
		IAbility a = null;
		if (num == 0) {
			a = avatar.abilityGun;
		} else if (num == 1) {
			a = avatar.abilityOther;
		} else {
			throw new RuntimeException("Unknown ability: " + num);
		}
		a.encode(this);
	}
	
	
}
