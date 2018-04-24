package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class AbilityActivatedMessage extends MyAbstractMessage {
	
	public int avatarID;
	public int abilityID;
	
	public AbilityActivatedMessage() {
		
	}

	
	public AbilityActivatedMessage(int _avatarID, int _abilityID) {
		avatarID = _avatarID;
		abilityID = _abilityID;
	}

}
