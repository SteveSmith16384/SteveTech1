package com.scs.stevetech1.components;

import com.jme3.scene.Spatial;

public interface IAvatarModel {

	Spatial createAndGetModel(boolean forClient);
	
	float getCameraHeight();
	
	float getBulletStartHeight();
	
}
