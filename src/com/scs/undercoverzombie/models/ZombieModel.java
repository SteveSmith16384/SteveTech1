package com.scs.undercoverzombie.models;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.scs.stevetech1.animation.IGetAvatarAnimationString;
import com.scs.stevetech1.jme.JMEFunctions;

/*
 * INFO: Found animation: ZombieAttack.
INFO: Found animation: ZombieBite.
INFO: Found animation: ZombieCrawl.
INFO: Found animation: ZombieIdle.
INFO: Found animation: ZombieRun.
INFO: Found animation: ZombieWalk.

 */
public class ZombieModel {

	private Spatial model;
	public AnimChannel channel;
	private AnimControl control;

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


	}
	
	
	public Spatial getModel() {
		return model;
	}

}
