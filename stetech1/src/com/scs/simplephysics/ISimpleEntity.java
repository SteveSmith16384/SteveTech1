package com.scs.simplephysics;

import com.jme3.scene.Spatial;

public interface ISimpleEntity<T> {

	Spatial getSpatial();
	
	void hasMoved();
	
	//todo - add this T getUserObject();
}
