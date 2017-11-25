package com.scs.simplephysics;

import com.jme3.math.Vector3f;

public interface ICollisionListener {

	boolean canCollide(SimpleRigidBody a, SimpleRigidBody b);
	
	void collisionOccurred(SimpleRigidBody a, SimpleRigidBody b, Vector3f point);
	
	void bodyOutOfBounds(SimpleRigidBody a);
	
}
