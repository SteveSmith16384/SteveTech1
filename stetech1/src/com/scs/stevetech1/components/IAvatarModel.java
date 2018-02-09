package com.scs.stevetech1.components;

import com.jme3.scene.Spatial;

public interface IAvatarModel {

	Spatial createAndGetModel(boolean forClient);
	
	float getCameraHeight();
	
	float getBulletStartHeight();
	
	//void setAnimationForCode(String code); // Called either server-side or client side for our own avatar.
	
	//void showCurrentAnimation();
	
	//void process(float tpf_secs);
		
}
