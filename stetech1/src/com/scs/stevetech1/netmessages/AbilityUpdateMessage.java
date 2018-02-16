package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.shared.IAbility;

@Serializable
public class AbilityUpdateMessage extends MyAbstractMessage {
	
	//public int bulletsLeftInMag;
	public float timeUntilShoot = 0;
	
	public int entityID;
	
	public AbilityUpdateMessage() {
		super();
	}


	public AbilityUpdateMessage(boolean reliable, IAbility ability) {
		super(reliable, true);
		
		//avatarId = avatar.id;
		//abilityNum = num;
		entityID = ability.getID();
		ability.encode(this);
	}
	
	
}
