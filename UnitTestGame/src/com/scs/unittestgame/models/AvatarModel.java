package com.scs.unittestgame.models;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.scs.stevetech1.components.IAvatarModel;

public class AvatarModel implements IAvatarModel {

	@Override
	public Spatial createAndGetModel() {
		return new Node("AvatarModel");
	}

	@Override
	public Spatial getModel() {
		return null;
	}

	@Override
	public Vector3f getSize() {
		return new Vector3f(1, 1, 1);
	}

	@Override
	public float getCameraHeight() {
		return 0;
	}

	@Override
	public float getBulletStartHeight() {
		return 0;
	}

	@Override
	public void setAnim(int anim) {
		
	}

}
