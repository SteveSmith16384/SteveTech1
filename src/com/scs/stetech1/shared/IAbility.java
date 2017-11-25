package com.scs.stetech1.shared;

import com.scs.stetech1.components.IProcessByServer;
import com.scs.stetech1.netmessages.AbilityUpdateMessage;

public interface IAbility extends IProcessByServer {

	/**
	 * Called every interval.
	 */
	//void process(float interpol);
	
	/**
	 * Called when activated.  Returns whether it was successfully activated.
	 */
	boolean activate(float interpol);
	
	String getHudText();
	
	void encode(AbilityUpdateMessage aum);

	void decode(AbilityUpdateMessage aum);
}

