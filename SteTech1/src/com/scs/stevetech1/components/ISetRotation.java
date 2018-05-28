package com.scs.stevetech1.components;

import com.jme3.math.Vector3f;

/**
 * Twinned with IGetRotation to point entities the right way.
 * These need to be implemented for entities to point in the right direction on the client.
 * @author stephencs
 *
 */
public interface ISetRotation {

	void setRotation(Vector3f dir);
}
