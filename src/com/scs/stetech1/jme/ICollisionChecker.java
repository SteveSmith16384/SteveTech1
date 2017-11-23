package com.scs.stetech1.jme;

import com.jme3.math.Ray;
import com.scs.stetech1.components.ICollideable;

public interface ICollisionChecker {

	boolean checkForCollisions(SimpleRigidBody entity);

	//boolean checkForCollisions(Ray ray);
}
