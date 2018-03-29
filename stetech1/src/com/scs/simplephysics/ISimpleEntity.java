package com.scs.simplephysics;

import com.jme3.bounding.BoundingBox;

public interface ISimpleEntity<T> {

	//Spatial getSpatial(); // todo - change the getBoundingBox?
	BoundingBox getBoundingBox();
	
	void hasMoved();
	
	//todo - add this T getUserObject();
}
