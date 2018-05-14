package com.scs.stevetech1.components;

import com.jme3.math.Vector3f;

/**
 * Twinned with ISetRotation to point entities the right way.
 * These need to be implemented for entities to point in the right direction on the client.
 *
 */
public interface IGetRotation {
	
	Vector3f getRotation();
	
}
