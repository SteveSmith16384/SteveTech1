package com.scs.stevetech1.components;

import com.jme3.math.Vector3f;

public interface IPhysicalEntity {

	Vector3f getWorldTranslation();
	
	void setWorldTranslation(Vector3f newPos);
	
	void adjustWorldTranslation(Vector3f offset);

}
