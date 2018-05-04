package com.scs.stevetech1.components;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public interface IAvatarModel {

	Spatial createAndGetModel(int side); // Only called client-side, since only the client uses full models.
	
	Spatial getModel();
	
	Vector3f getSize();
	
	float getCameraHeight();
	
	float getBulletStartHeight();
	
	void setAnim(int anim);
	
}
