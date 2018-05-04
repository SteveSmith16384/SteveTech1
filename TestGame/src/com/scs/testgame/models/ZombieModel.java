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
import com.scs.stevetech1.jme.JMEModelFunctions;

/*
INFO: Found animation: ZombieAttack.
INFO: Found animation: ZombieBite.
INFO: Found animation: ZombieCrawl.
INFO: Found animation: ZombieIdle.
INFO: Found animation: ZombieRun.
INFO: Found animation: ZombieWalk.

 */
public class ZombieModel implements IAvatarModel {

	private static final float ZOMBIE_MODEL_WIDTH = .3f;
	private static final float ZOMBIE_MODEL_DEPTH = .3f;
	private static final float ZOMBIE_MODEL_HEIGHT = .7f;

	public AnimChannel channel;
	private HashMap<String, String> animCodes = new HashMap<String, String>();
	private AssetManager assetManager;
	private Spatial model;


	public ZombieModel(AssetManager _assetManager) {
		assetManager = _assetManager;

		animCodes.put("Idle", "ZombieIdle");
		animCodes.put("Walking", "ZombieWalk");
	}


	@Override
	public Spatial createAndGetModel(int side) {
		model = assetManager.loadModel("Models/zombie/Zombie.blend");
		model.scale(.125f); // Make 1 high
		model.setModelBound(new BoundingBox());
		//model.updateModelBound();

		JMEModelFunctions.setTextureOnSpatial(assetManager, model, "Models/zombie/ZombieTexture.png");

		Node s = (Node)model;
		while (s.getNumControls() == 0) {
			s = (Node)s.getChild(0);
		}
		AnimControl control = s.getControl(AnimControl.class);
		channel = control.createChannel();

		/*
			Quaternion target_q = new Quaternion();
			target_q.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
			model.setLocalRotation(target_q);
		 */
		return model;
	}


	@Override
	public float getCameraHeight() {
		return ZOMBIE_MODEL_HEIGHT;
	}


	@Override
	public float getBulletStartHeight() {
		return ZOMBIE_MODEL_HEIGHT - 0.1f;
	}


	@Override
	public Vector3f getSize() {
		return new Vector3f(ZOMBIE_MODEL_WIDTH, ZOMBIE_MODEL_HEIGHT, ZOMBIE_MODEL_DEPTH);
	}


	@Override
	public Spatial getModel() {
		return model;
	}


	@Override
	public void setAnim(int anim) {
		
	}

}
