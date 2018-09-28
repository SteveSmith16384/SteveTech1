package com.scs.stevetech1.components;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 * Interface for classes that can be used for a player's avatar.
 * @author stephencs
 *
 */
public interface IAvatarModel {

	Spatial createAndGetModel(); // Only called client-side, since only the client uses full models.
	
	Spatial getModel(); // Only called client-side, since only the client uses full models.
	
	/**
	 * @return The size of the model for collision purposes.
	 */
	Vector3f getCollisionBoxSize();
	
	/**
	 * 
	 * @return The height of the camera from the bottom of the model.
	 */
	float getCameraHeight();
	
	float getBulletStartHeight(); // todo - change to bulletStartOffset.  Or remove this and use ICanShoot.bulletStartPos!
	
	void setAnim(int anim);
	
}
