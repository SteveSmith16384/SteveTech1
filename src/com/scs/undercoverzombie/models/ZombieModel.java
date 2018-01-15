package com.scs.undercoverzombie.models;

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
import com.scs.stevetech1.components.IAnimatedAvatar;
import com.scs.stevetech1.jme.JMEFunctions;

/*
 * INFO: Found animation: ZombieAttack.
INFO: Found animation: ZombieBite.
INFO: Found animation: ZombieCrawl.
INFO: Found animation: ZombieIdle.
INFO: Found animation: ZombieRun.
INFO: Found animation: ZombieWalk.

 */
public class ZombieModel implements IAnimatedAvatar {

	public static final float ZOMBIE_MODEL_WIDTH = .3f;
	public static final float ZOMBIE_MODEL_DEPTH = .3f;
	public static final float ZOMBIE_MODEL_HEIGHT = .7f;

	private Spatial model;
	public AnimChannel channel;
	private AnimControl control;
	private HashMap<String, String> animCodes = new HashMap<String, String>();

	public ZombieModel(AssetManager assetManager) {
		model = assetManager.loadModel("Models/zombie/Zombie.blend");
		model.scale(.125f); // Make 1 high
		JMEFunctions.SetTextureOnSpatial(assetManager, model, "Models/zombie/ZombieTexture.png");
		model.setModelBound(new BoundingBox());
		model.updateModelBound();

		Node s = (Node)model;
		while (s.getNumControls() == 0) {
			s = (Node)s.getChild(0);
		}
		control = s.getControl(AnimControl.class);
		channel = control.createChannel();
		//channel.setAnim("ZombieWalk");

		Quaternion target_q = new Quaternion();
		target_q.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y); // todo - was -1, 0, 0
		model.setLocalRotation(target_q);

		animCodes.put("Idle", "ZombieIdle");
		animCodes.put("Walking", "ZombieWalk");

	}


	public Spatial getModel(boolean forClient) {
		if (forClient) {
			return model;
		} else {
			Box box1 = new Box(ZombieModel.ZOMBIE_MODEL_WIDTH/2, ZombieModel.ZOMBIE_MODEL_HEIGHT/2, ZombieModel.ZOMBIE_MODEL_DEPTH/2);
			Geometry geometry = new Geometry("Crate", box1);
			geometry.setLocalTranslation(0, ZombieModel.ZOMBIE_MODEL_HEIGHT/2, 0); // Move origin to floor
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

/*
	@Override
	public Vector3f getModelDimensions() {
		// TODO Auto-generated method stub
		return null;
	}
*/

}
