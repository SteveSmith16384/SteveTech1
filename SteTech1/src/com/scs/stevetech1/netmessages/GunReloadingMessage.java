package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.shared.IAbility;

/**
 * Server says that the weapon is being reloaded
 *
 */
@Serializable
public class GunReloadingMessage extends MyAbstractMessage { // todo - do we need this?
	
	public int abilityId;
	public float durationSecs;
	
	public GunReloadingMessage() {
		super();
	}


	public GunReloadingMessage(IAbility ability, float _duration) {
		super(true, false); // Not scheduled since the player wants to see anim straight away
		
		abilityId = ability.getID();
		durationSecs = _duration;
	}

}

