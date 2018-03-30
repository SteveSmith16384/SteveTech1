package com.scs.simplephysics.tests;

import com.jme3.collision.Collidable;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.scs.simplephysics.ISimpleEntity;

/**
 * This is a helper class for creating an instance of ISimpleEntity from a Spatial.
 * @author carlylesmith
 *
 * @param <T>
 */
public class SimpleEntityHelper<T> implements ISimpleEntity<T> {
	
	private Spatial spatial;
	
	public SimpleEntityHelper(Spatial s) {
		spatial = s;
	}

	@Override
	public Collidable getCollidable() {
		return spatial.getWorldBound();
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
