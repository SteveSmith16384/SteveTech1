package com.scs.simplephysics;

import java.util.Collection;

import com.jme3.math.Vector3f;

public interface ICollisionListener {

	void collisionOccurred(SimpleRigidBody a, SimpleRigidBody b, Vector3f point);
	
}
