package com.scs.stetech1.shared;

import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.netmessages.AbilityUpdateMessage;

public interface IAbility extends IEntity {

	/**
	 * Called when activated/used.  Returns whether it was successfully used.
	 */
	boolean activate();
	
	String getHudText();
	
	void encode(AbilityUpdateMessage aum);

	void decode(AbilityUpdateMessage aum);
	
	long getLastUpdateTime();
	
	void setLastUpdateTime(long l);
}

