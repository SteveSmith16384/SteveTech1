package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

/**
 * Sent from client to server to tell it that the weapon has been fired.
 *
 */
@Serializable
public class AbilityActivatedMessage extends MyAbstractMessage {
	
	public int avatarID;
	public int abilityID;
	
	public AbilityActivatedMessage() {
		
	}

	
	public AbilityActivatedMessage(int _avatarID, int _abilityID) {
		super(true, false);
		
		avatarID = _avatarID;
		abilityID = _abilityID;
	}

}
