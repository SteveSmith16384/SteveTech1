package com.scs.stetech1.shared;

import com.scs.stetech1.components.IProcessByServer;
import com.scs.stetech1.netmessages.AbilityUpdateMessage;

public interface IAbility {//extends IProcessByServer {

	/**
	 * Called when activated/used.  Returns whether it was successfully used.
	 */
	boolean activate(float interpol);
	
	void process(float tpf_secs);
	
	String getHudText();
	
	void encode(AbilityUpdateMessage aum);

	void decode(AbilityUpdateMessage aum);
}

