package com.scs.simplephysics;

import com.jme3.scene.Spatial;

public interface ISimpleEntity<T> {

	Spatial getSpatial(); // todo - change the getBoundingBox?
	
	void hasMoved();
	
	//todo - add this T getUserObject();
}
