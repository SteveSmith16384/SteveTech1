package com.scs.stetech1.components;

import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;

/**
 * Implement this if you want to run special code when entity collides.
 *
 */
public interface ICollideable extends Collidable { // todo - rename
	
	BoundingVolume getBoundingVolume();

	//Collidable getCollidable();
	
	void collidedWith(ICollideable other);
		
}
