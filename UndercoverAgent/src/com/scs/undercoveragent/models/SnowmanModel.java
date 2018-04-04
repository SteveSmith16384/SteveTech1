package com.scs.undercoveragent.models;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Spatial;
import com.scs.stevetech1.components.IAvatarModel;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.jme.JMEModelFunctions;

/*
 * This class, and classes like this, are designed to keep all the model-specific settings in one place.
 */
public class SnowmanModel implements IAvatarModel {

	private static final float MODEL_WIDTH = 0.4f;
	private static final float MODEL_DEPTH = 0.3f;
	private static final float MODEL_HEIGHT = 0.7f;

	private AssetManager assetManager;
	private Spatial model;
	private Vector3f origPos;

	private boolean showingDied = false;

	public SnowmanModel(AssetManager _assetManager) {
		assetManager = _assetManager;

	}


	@Override
	public Spatial createAndGetModel(boolean forClient, int side) {
		//if (forClient && Globals.USE_SERVER_MODELS_ON_CLIENT == false) {
			model = assetManager.loadModel("Models/Holiday/Snowman.obj");
			model.setShadowMode(ShadowMode.CastAndReceive);
			JMEModelFunctions.scaleModelToHeight(model, MODEL_HEIGHT);
			JMEModelFunctions.moveYOriginTo(model, 0f);
			JMEAngleFunctions.rotateToDirection(model, new Vector3f(-1, 0, 0)); // Point model fwds
			
			origPos = model.getLocalTranslation().clone();
			
			return model;
		/*} else {
			Box box1 = new Box(MODEL_WIDTH/2, MODEL_HEIGHT/2, MODEL_DEPTH/2);
			model = new Geometry("Snowman", box1);
			model.setLocalTranslation(0, MODEL_HEIGHT/2, 0); // Move origin to floor

			if (Globals.USE_SERVER_MODELS_ON_CLIENT) {
				// Need to give it a tex
				JMEFunctions.setTextureOnSpatial(assetManager, model, "Textures/greensun.jpg");
			}

			return model;
		}*/
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
		/*if (Globals.DEBUG_ANIM) {
			Globals.p("Snowman sinking...");
		}*/
		try {
			if (model != null) { this.model.getLocalTranslation();
				this.model.move(0, -tpf_secs/7, 0);
			}
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
		showingDied = true;

	}


	public void showAlive(float tpf_secs) {
		//model.getLocalTranslation();
		//model.getParent().getLocalTranslation();
		if (!showingDied) {
			return;
		} 
		this.model.setLocalTranslation(origPos);
		this.model.updateGeometricState();
		showingDied = false;

	}


	@Override
	public BoundingBox getBoundingBox() {
		return new BoundingBox(new Vector3f(), MODEL_WIDTH/2, MODEL_HEIGHT/2, MODEL_DEPTH/2);
	}
}
