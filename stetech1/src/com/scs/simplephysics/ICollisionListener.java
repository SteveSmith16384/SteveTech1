package com.scs.simplephysics;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public interface ICollisionListener<T> {

	/**
	 * Typically, this should return true unless you don't want the entities to collide.
	 */
	boolean canCollide(SimpleRigidBody<T> a, SimpleRigidBody<T> b);
	
	/**
	 * Notify that a collision has occurred.
	 */
	void collisionOccurred(SimpleRigidBody<T> a, SimpleRigidBody<T> b, Vector3f point); // todo - remove point
	
}
