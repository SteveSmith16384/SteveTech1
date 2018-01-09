package com.scs.undercoverzombie.models;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
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
		//AnimControl ac = (AnimControl)s.getControl(AnimControl.class);
		control = s.getControl(AnimControl.class);
		//control.addListener(this);
		channel = control.createChannel();
		//channel.setAnim("ZombieWalk");


	}
	
	
	public Spatial getModel() {
		return model;
	}

}
