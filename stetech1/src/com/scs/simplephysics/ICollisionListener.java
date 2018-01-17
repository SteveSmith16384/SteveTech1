package com.scs.simplephysics;

import com.jme3.math.Vector3f;

public interface ICollisionListener<T> {

	/*
	 * Typically, this should return true unless you don't want the entities to collide.
	 */
	boolean canCollide(SimpleRigidBody<T> a, SimpleRigidBody<T> b);
	
	void collisionOccurred(SimpleRigidBody<T> a, SimpleRigidBody<T> b, Vector3f point);
	
}
