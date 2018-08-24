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
	
	Spatial getModel();
	
	Vector3f getSize();
	
	float getCameraHeight();
	
	float getBulletStartHeight(); // todo - change to bulletStartOffset.  Or remove this and use ICanShoot.bulletStartPos!
	
	void setAnim(int anim);
	
}
