package com.scs.stevetech1.components;

/**
 * Prevents entities on the same side from colliding, e.g. two avatars, shooters with their bullets, friendly fire etc...
 * @author stephencs
 *
 */
public interface IDontCollideWithComrades {

	int getSide();
	
}
