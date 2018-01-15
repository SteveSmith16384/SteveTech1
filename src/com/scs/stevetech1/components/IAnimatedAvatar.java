package com.scs.stevetech1.components;

import com.jme3.scene.Spatial;

public interface IAnimatedAvatar { // todo - rename to ...

	Spatial getModel(boolean forClient);
	
	float getCameraHeight();
	
	float getBulletStartHeight();
	
	String getAnimationStringForCode(String code);
	
	//Vector3f getModelDimensions();

}
