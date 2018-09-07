package com.scs.stevetech1.components;

/**
 * Used by the AI to determine if something is a valid target for it.
 * @author stephencs
 *
 */
public interface ITargetableByAI {

	boolean isValidTargetForSide(byte shootersSide);
	
	boolean isAlive();
	
	int getTargetPriority();
	
}
