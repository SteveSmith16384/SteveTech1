package com.scs.testgame.models;

import java.util.HashMap;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.scs.stevetech1.components.IAvatarModel;

public class CharacterModel implements IAvatarModel {

	private static final float MODEL_WIDTH = .3f;
	private static final float MODEL_DEPTH = .3f;
	private static final float MODEL_HEIGHT = .7f;

	public AnimChannel channel;
	private HashMap<String, String> animCodes = new HashMap<String, String>();
	private AssetManager assetManager;
	
	public CharacterModel(AssetManager _assetManager) {
		assetManager = _assetManager;
		
		animCodes.put("Idle", "IDLE1");
		animCodes.put("Walking", "WALK");
	}


	@Override
	public Spatial createAndGetModel(boolean forClient, int side) {
		//if (forClient) {
			Spatial model = assetManager.loadModel("Models/3d-character/character/character.blend");
			model.scale(.125f); // Make 1 high
			model.setModelBound(new BoundingBox());
			//model.updateModelBound();

			Node s = (Node)model;
			while (s.getNumControls() == 0) {
				s = (Node)s.getChild(0);
			}
			AnimControl control = s.getControl(AnimControl.class);
			channel = control.createChannel();
			return model;
		/*} else {
			Box box1 = new Box(MODEL_WIDTH/2, MODEL_HEIGHT/2, MODEL_DEPTH/2);
			Geometry geometry = new Geometry("CharacterBox", box1);
			geometry.setLocalTranslation(0, MODEL_HEIGHT/2, 0); // Move origin to floor
			return geometry;
		}*/
	}

/*
	@Override
	public void setAnimationForCode(String code) {
		//return animCodes.get(code);
	}

*/
	@Override
	public float getCameraHeight() {
		return MODEL_HEIGHT;
	}


	@Override
	public float getBulletStartHeight() {
		return MODEL_HEIGHT - 0.1f;
	}


	@Override
	public BoundingBox getBoundingBox() {
		return new BoundingBox(new Vector3f(), MODEL_WIDTH/2, MODEL_HEIGHT/2, MODEL_DEPTH/2);
	}
}
