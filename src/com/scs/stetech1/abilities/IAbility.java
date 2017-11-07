package com.scs.stetech1.abilities;

import com.scs.stetech1.components.IProcessByServer;

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
}

