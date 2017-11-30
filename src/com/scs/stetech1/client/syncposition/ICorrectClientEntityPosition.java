package com.scs.stetech1.client.syncposition;

import com.jme3.math.Vector3f;
import com.scs.stetech1.components.IPhysicalEntity;

/*
 * Interface for implementing algorythms for correcting the client entity position when it is different to what the server says it should be.
 */
public interface ICorrectClientEntityPosition {

	void adjustPosition(IPhysicalEntity avatar, Vector3f offset);
	
}
