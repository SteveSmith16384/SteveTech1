package com.scs.simplephysics.tests;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.scs.simplephysics.ISimpleEntity;

public class SimpleEntity<T> implements ISimpleEntity<T> {
	
	private Spatial spatial;
	
	public SimpleEntity(Spatial s) {
		spatial = s;
	}

	@Override
	public BoundingBox getBoundingBox() {
		return (BoundingBox)spatial.getWorldBound();
	}

	@Override
	public void moveEntity(Vector3f pos) {
		this.spatial.move(pos);
		
	}

	@Override
	public void hasMoved() {
		// Do nothing
		
	}

}
