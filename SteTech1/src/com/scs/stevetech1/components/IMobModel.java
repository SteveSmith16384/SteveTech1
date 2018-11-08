package com.scs.stevetech1.components;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public interface IMobModel {

	Spatial createAndGetModel(); // Only called client-side, since only the client uses full models.
	
	Spatial getModel(); // Only called client-side, since only the client uses full models.
	
	/**
	 * @return The size of the model for collision purposes.
	 */
	Vector3f getCollisionBoxSize();
	
	void setAnim(int anim);
	
	float getBulletStartHeight(); // todo - change to bulletStartOffset.  Or remove this and use ICanShoot.bulletStartPos!
	
}
