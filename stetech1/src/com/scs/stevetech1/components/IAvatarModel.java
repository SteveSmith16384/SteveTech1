package com.scs.stevetech1.components;

import com.jme3.animation.AnimEventListener;
import com.jme3.scene.Spatial;

public interface IAvatarModel {

	Spatial createAndGetModel(boolean forClient, int side);
	
	float getCameraHeight();
	
	float getBulletStartHeight();
	
}
