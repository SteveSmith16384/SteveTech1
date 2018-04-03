package com.scs.simplephysics;

public interface ICollisionListener<T> {

	/**
	 * Typically, this should return true unless you don't want the entities to collide.
	 */
	boolean canCollide(SimpleRigidBody<T> a, SimpleRigidBody<T> b);
	
	/**
	 * Notify that a collision has occurred between two SimpleRigidBodies.
	 */
	void collisionOccurred(SimpleRigidBody<T> a, SimpleRigidBody<T> b);
	
}
