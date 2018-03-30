package com.scs.simplephysics;

import com.jme3.collision.Collidable;
import com.jme3.math.Vector3f;

public interface ISimpleEntity<T> {

	Collidable getCollidable(); // For collision detection
	
	void moveEntity(Vector3f pos);
	
	void hasMoved();
	
	//todo - add this T getUserObject();
}
