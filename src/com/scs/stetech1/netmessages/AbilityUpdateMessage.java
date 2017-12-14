package com.scs.stetech1.netmessages;

import com.jme3.network.serializing.Serializable;
import com.scs.stetech1.entities.AbstractAvatar;
import com.scs.stetech1.shared.IAbility;

@Serializable
public class AbilityUpdateMessage extends MyAbstractMessage {
	
	public int bulletsLeftInMag;
	public float timeUntilShoot = 0;
	
	public int entityID; //
	//public int avatarId = -1;
	//public int abilityNum = -1; // 0 = main etc...
	
	public AbilityUpdateMessage() {
		super(true);
	}


	public AbilityUpdateMessage(boolean reliable, IAbility ability) {
		super(reliable);
		
		//avatarId = avatar.id;
		//abilityNum = num;
		entityID = ability.getID();
		ability.encode(this);
	}
	
	
}
