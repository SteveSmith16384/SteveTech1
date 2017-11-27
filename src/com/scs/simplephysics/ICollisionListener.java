package com.scs.simplephysics;

import com.jme3.math.Vector3f;

public interface ICollisionListener<T> {

	boolean canCollide(SimpleRigidBody<T> a, SimpleRigidBody<T> b);
	
	void collisionOccurred(SimpleRigidBody<T> a, SimpleRigidBody<T> b, Vector3f point);
	
	void bodyOutOfBounds(SimpleRigidBody<T> a);
	
}
