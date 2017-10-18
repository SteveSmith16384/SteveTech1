package com.scs.stetech1.components;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public interface ISharedEntity extends IEntity {
	
	Vector3f getLocalTranslation();
	
	Quaternion getRotation();
	
	boolean canMove();

}
