package com.scs.stetech1.components;


/**
 * Implement this if you want to run special code when entity  collides.
 *
 */
public interface ICollideable {

	void collidedWith(ICollideable other);
		
}
