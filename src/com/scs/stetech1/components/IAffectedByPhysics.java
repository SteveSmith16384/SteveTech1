package com.scs.stetech1.components;

import com.jme3.math.Vector3f;

/**
 * Implement this to be able to manually apply forces to an entity, e.g. if caught in an explosion.
 *
 */
public interface IAffectedByPhysics {

	Vector3f getWorldTranslation();
	
	void applyOneOffForce(Vector3f dir);
}
