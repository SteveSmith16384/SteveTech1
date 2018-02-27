package com.scs.moonbaseassault.models;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.scs.stevetech1.components.IAvatarModel;
import com.scs.stevetech1.jme.JMEFunctions;
import com.scs.stevetech1.server.Globals;

//Punch, Walk, Working, ArmatureAction.002, Idle, Death, Run, Jump
public class SoldierModel implements IAvatarModel {

	private static final float MODEL_WIDTH = 0.4f;
	private static final float MODEL_DEPTH = 0.3f;
	private static final float MODEL_HEIGHT = 0.7f;

	private AssetManager assetManager;
	private Spatial model;
	private Vector3f origPos;

	private boolean showingDied = false;

	public SoldierModel(AssetManager _assetManager) {
		assetManager = _assetManager;

	}


	@Override
	public Spatial createAndGetModel(boolean forClient) {
		if (forClient && Globals.USE_SERVER_MODELS_ON_CLIENT == false) {
			model = assetManager.loadModel("Models/AnimatedHuman/Animated Human.blend");
			JMEFunctions.setTextureOnSpatial(assetManager, model, "Models/AnimatedHuman/Textures/ClothedDarkSkin2.png");
			JMEFunctions.scaleModelToHeight(model, MODEL_HEIGHT);
			JMEFunctions.moveYOriginTo(model, 0f);
			JMEFunctions.rotateToDirection(model, new Vector3f(-1, 0, 0)); // Point model fwds
			
			origPos = model.getLocalTranslation().clone();
			
			return model;
		} else {
			Box box1 = new Box(MODEL_WIDTH/2, MODEL_HEIGHT/2, MODEL_DEPTH/2);
			model = new Geometry("Soldier", box1);
			model.setLocalTranslation(0, MODEL_HEIGHT/2, 0); // Move origin to floor

			if (Globals.USE_SERVER_MODELS_ON_CLIENT) {
				// Need to give it a tex
				JMEFunctions.setTextureOnSpatial(assetManager, model, "Textures/greensun.jpg");
			}

			return model;
		}
	}


	@Override
	public float getCameraHeight() {
		return MODEL_HEIGHT - 0.2f;
	}


	@Override
	public float getBulletStartHeight() {
		return MODEL_HEIGHT - 0.3f;
	}


	public void showDied(float tpf_secs) {
		// todo
		showingDied = true;

	}


	public void showAlive(float tpf_secs) {
		if (!showingDied) {
			return;
		} 
		// todo
		showingDied = false;

	}
}
