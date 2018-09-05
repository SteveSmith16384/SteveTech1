package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.shared.IAbility;

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

	
	public AbilityActivatedMessage(AbstractAvatar _avatar, IAbility ability) {
		super(true, false);
		
		avatarID = _avatar.getID();
		abilityID = ability.getID();
	}

}
