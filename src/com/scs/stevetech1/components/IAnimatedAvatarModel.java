package com.scs.stevetech1.components;

import com.jme3.scene.Spatial;

public interface IAnimatedAvatarModel {

	Spatial getModel(boolean forClient);
	
	float getCameraHeight();
	
	float getBulletStartHeight();
	
	String getAnimationStringForCode(String code);
	
}
