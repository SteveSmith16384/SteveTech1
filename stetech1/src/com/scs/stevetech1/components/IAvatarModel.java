package com.scs.stevetech1.components;

import com.jme3.bounding.BoundingBox;
import com.jme3.scene.Spatial;

public interface IAvatarModel {

	Spatial createAndGetModel(boolean forClient, int side);
	
	BoundingBox getBoundingBox(); // todo - change to Vector3f getSize();
	
	float getCameraHeight();
	
	float getBulletStartHeight();
	
}
