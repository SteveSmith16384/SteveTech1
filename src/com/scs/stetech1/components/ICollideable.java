package com.scs.stetech1.components;

import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;

/**
 * Implement this if you want to run special code when entity collides.
 *
 */
public interface ICollideable extends Collidable {
	
	BoundingVolume getBoundingVolume();

	/*
	 * Code to run if the entity collides with another.
	 * Return false if entity must move back.
	 */
	boolean collidedWith(ICollideable other);
	
}
