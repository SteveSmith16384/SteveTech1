package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.shared.IAbility;

@Serializable
public class AbilityReloadingMessage extends MyAbstractMessage {
	
	//public int avatarID;
	public int abilityID;
	
	public AbilityReloadingMessage() {
		
	}

	
	public AbilityReloadingMessage(IAbility ability) {
		super(false, false);
		
		//avatarID = _avatar.getID();
		abilityID = ability.getID();
	}

}
