package com.scs.stevetech1.shared;

import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.netmessages.AbilityUpdateMessage;

/*
 * This is the interface for all weapons and abilities. 
 */
public interface IAbility extends IEntity {

	String getName();
	
	/**
	 * Called when activated/used.  Returns whether it was successfully used.
	 */
	boolean activate();
	
	String getHudText();
	
	//String getAvatarAnimationCode();
	
	void encode(AbilityUpdateMessage aum);

	void decode(AbilityUpdateMessage aum);
	
	long getLastUpdateTime();
	
	void setLastUpdateTime(long l);
}

