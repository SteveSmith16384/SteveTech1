package com.scs.stevetech1.components;

/**
 * Bullets don't collide with each other, or with their shooter
 *
 */
public interface IBullet {

	IEntity getLauncher(); // So we know who not to collide with
	

}
