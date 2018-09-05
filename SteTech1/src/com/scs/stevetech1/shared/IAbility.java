package com.scs.stevetech1.shared;

import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.netmessages.AbilityUpdateMessage;

/*
 * This is the interface for all weapons and abilities. 
 */
public interface IAbility extends IEntity {

	/**
	 * Indicates that a the server has received an AbilityActivatedMessage msg, so the ability will be activated in the game loop.
	 */
	void setToBeActivated(boolean b);
	
	boolean isGoingToBeActivated();
	
	/**
	 * Called when activated/used.  Returns whether it was successfully used.
	 */
	boolean activate();
	
	
	String getHudText();
	
	void encode(AbilityUpdateMessage aum); // todo - rename

	void decode(AbilityUpdateMessage aum); // todo - rename
	
	
	/**
	 * To check that we only use the latest update message
	 * @return
	 */
	long getLastUpdateTime();
	
	void setLastUpdateTime(long l);
}

