package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.shared.IAbility;

/**
 * Server says that the weapon is being reloaded
 *
 */
@Serializable
public class GunReloadingMessage extends MyAbstractMessage {
	
	public int abilityId;
	public float duration_secs;
	
	public GunReloadingMessage() {
		super();
	}


	public GunReloadingMessage(IAbility ability, float _duration) {
		super(true, true);
		
		abilityId = ability.getID();
		duration_secs = _duration;
	}

}

