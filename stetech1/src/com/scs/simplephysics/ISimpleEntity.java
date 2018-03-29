package com.scs.simplephysics;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

public interface ISimpleEntity<T> {

	//Spatial getSpatial(); // todo - change the getBoundingBox?
	BoundingBox getBoundingBox();
	
	void moveEntity(Vector3f pos);
	
	void hasMoved();
	
	//todo - add this T getUserObject();
}
