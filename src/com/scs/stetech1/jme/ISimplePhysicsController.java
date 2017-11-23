package com.scs.stetech1.jme;

import java.util.Iterator;

public interface ISimplePhysicsController {

	Iterator<Object> getEntities(); // todo - use T?

	void collisionOccurred(SimpleRigidBody a, Object b);
	
}
