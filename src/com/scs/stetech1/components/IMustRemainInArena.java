package com.scs.stetech1.components;

import com.jme3.math.Vector3f;

public interface IMustRemainInArena {

	Vector3f getWorldTranslation();
	
	void remove();
	
	void respawn();
}
