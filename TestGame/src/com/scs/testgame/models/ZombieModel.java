package com.scs.testgame.models;

import java.util.HashMap;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.scs.stevetech1.components.IAnimatedAvatarModel;
import com.scs.stevetech1.jme.JMEFunctions;

/*
INFO: Found animation: ZombieAttack.
INFO: Found animation: ZombieBite.
INFO: Found animation: ZombieCrawl.
INFO: Found animation: ZombieIdle.
INFO: Found animation: ZombieRun.
INFO: Found animation: ZombieWalk.

 */
public class ZombieModel implements IAnimatedAvatarModel {

	private static final float ZOMBIE_MODEL_WIDTH = .3f;
	private static final float ZOMBIE_MODEL_DEPTH = .3f;
	private static final float ZOMBIE_MODEL_HEIGHT = .7f;

	public AnimChannel channel;
	private HashMap<String, String> animCodes = new HashMap<String, String>();
	private AssetManager assetManager;
	
	public ZombieModel(AssetManager _assetManager) {
		assetManager = _assetManager;
		
		animCodes.put("Idle", "ZombieIdle");
		animCodes.put("Walking", "ZombieWalk");
	}


	@Override
	public Spatial getModel(boolean forClient) {
		if (forClient) {
			Spatial model = assetManager.loadModel("Models/zombie/Zombie.blend");
			model.scale(.125f); // Make 1 high
			model.setModelBound(new BoundingBox());
			//model.updateModelBound();

			JMEFunctions.SetTextureOnSpatial(assetManager, model, "Models/zombie/ZombieTexture.png");

			Node s = (Node)model;
			while (s.getNumControls() == 0) {
				s = (Node)s.getChild(0);
			}
			AnimControl control = s.getControl(AnimControl.class);
			channel = control.createChannel();

			/*
			Quaternion target_q = new Quaternion();
			target_q.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y); // todo - was -1, 0, 0
			model.setLocalRotation(target_q);
	*/
			return model;
		} else {
			Box box1 = new Box(ZOMBIE_MODEL_WIDTH/2, ZOMBIE_MODEL_HEIGHT/2, ZOMBIE_MODEL_DEPTH/2);
			Geometry geometry = new Geometry("ZombieBox", box1);
			geometry.setLocalTranslation(0, ZOMBIE_MODEL_HEIGHT/2, 0); // Move origin to floor
			return geometry;
		}
	}


	@Override
	public String getAnimationStringForCode(String code) {
		return animCodes.get(code);
	}


	@Override
	public float getCameraHeight() {
		return ZOMBIE_MODEL_HEIGHT;
	}


	@Override
	public float getBulletStartHeight() {
		return ZOMBIE_MODEL_HEIGHT - 0.1f;
	}

}
