package com.scs.undercoveragent.models;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.scs.stevetech1.components.IAnimatedAvatarModel;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.jme.JMEFunctions;
import com.scs.stevetech1.server.Globals;

/*
 * This class, and classes like this, is designed to keep all the model-specific settings in one place.
 */
public class SnowmanModel implements IAnimatedAvatarModel {

	private static final float MODEL_WIDTH = 0.4f;
	private static final float MODEL_DEPTH = 0.3f;
	private static final float MODEL_HEIGHT = 0.7f;

	private AssetManager assetManager;
	private Spatial model;

	private boolean showDied = false;

	public SnowmanModel(AssetManager _assetManager) {
		assetManager = _assetManager;

	}


	@Override
	public Spatial getModel(boolean forClient) {
		if (forClient && Globals.USE_SERVER_MODELS_ON_CLIENT == false) {
			model = assetManager.loadModel("Models/Holiday/Snowman.obj");
			JMEFunctions.scaleModelToHeight(model, MODEL_HEIGHT);
			JMEFunctions.moveYOriginTo(model, 0f);
			JMEFunctions.RotateToDirection(model, new Vector3f(-1, 0, 0)); // Point model fwds
			return model;
		} else {
			Box box1 = new Box(MODEL_WIDTH/2, MODEL_HEIGHT/2, MODEL_DEPTH/2);
			Geometry geometry = new Geometry("Snowman", box1);
			geometry.setLocalTranslation(0, MODEL_HEIGHT/2, 0); // Move origin to floor

			if (Globals.USE_SERVER_MODELS_ON_CLIENT) {
				// Need to give it a tex
				JMEFunctions.SetTextureOnSpatial(assetManager, geometry, "Textures/greensun.jpg");
			}

			return geometry;
		}
	}


	@Override
	public void setAnimationForCode(String code) {
		if (code == AbstractAvatar.ANIM_DIED) {
			showDied = true;
		}
	}


	@Override
	public float getCameraHeight() {
		return MODEL_HEIGHT - 0.1f;
	}


	@Override
	public float getBulletStartHeight() {
		return MODEL_HEIGHT - 0.2f;
	}


	@Override
	public void showCurrentAnimation() {
		if (showDied) {
			showDied = false;
			JMEFunctions.RotateToDirection(this.model, new Vector3f(0, 1, 0));
		}

	}

}
