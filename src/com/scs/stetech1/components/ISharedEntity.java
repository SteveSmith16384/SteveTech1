package com.scs.stetech1.components;

import java.util.HashMap;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public interface ISharedEntity extends IEntity {
	
	HashMap<String, Object> getCreationData();
	
	Vector3f getLocalTranslation();
	
	Quaternion getRotation();
	
	boolean canMove();

}
