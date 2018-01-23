package com.scs.undercoveragent.models;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.scs.stevetech1.components.IAnimatedAvatarModel;

public class SnowmanModel implements IAnimatedAvatarModel {

	private static final float MODEL_WIDTH = .5f;
	private static final float MODEL_DEPTH = 1f;
	private static final float MODEL_HEIGHT = 0.97f;

	private AssetManager assetManager;
	
	public SnowmanModel(AssetManager _assetManager) {
		assetManager = _assetManager;
		
	}


	@Override
	public Spatial getModel(boolean forClient) {
		if (forClient) {
			Spatial model = assetManager.loadModel("Models/Holiday/Snowman.obj");
			model.setLocalTranslation(0, .3f, 0);
			model.scale(.36f);
			return model;
		} else {
			Box box1 = new Box(MODEL_WIDTH/2, MODEL_HEIGHT/2, MODEL_DEPTH/2);
			Geometry geometry = new Geometry("Snowman", box1);
			geometry.setLocalTranslation(0, MODEL_HEIGHT/2, 0); // Move origin to floor
			return geometry;
		}
	}


	@Override
	public String getAnimationStringForCode(String code) {
		return null; // No animation
	}


	@Override
	public float getCameraHeight() {
		return MODEL_HEIGHT;
	}


	@Override
	public float getBulletStartHeight() {
		return MODEL_HEIGHT - 0.1f;
	}

}
