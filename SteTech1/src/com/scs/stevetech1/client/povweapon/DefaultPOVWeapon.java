package com.scs.stevetech1.client.povweapon;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.jme.JMEModelFunctions;
import com.scs.stevetech1.server.Globals;

public class DefaultPOVWeapon implements IPOVWeapon {

	private AbstractGameClient client;
	private Node playersWeaponNode;
	private Spatial weaponModel;
	private float finishedReloadAt;
	private boolean currentlyReloading = false;
	private float gunAngle = 0;

	
	public DefaultPOVWeapon(AbstractGameClient _client) {
		super();
		
		client = _client;
		
		Spatial model = client.getAssetManager().loadModel("Models/pistol/pistol.blend");
		JMEModelFunctions.setTextureOnSpatial(client.getAssetManager(), model, "Models/pistol/pistol_tex.png");
		model.scale(0.1f);
		// x moves l-r, z moves further away
		//model.setLocalTranslation(-0.20f, -.2f, 0.4f);
		//model.setLocalTranslation(-0.15f, -.2f, 0.2f);
		model.setLocalTranslation(-0.10f, -.15f, 0.2f);
		
		weaponModel = model;
		playersWeaponNode = new Node("weaponParent");
		playersWeaponNode.attachChild(weaponModel);

	}
	
	
	@Override
	public void update(float tpfSecs) {
		Camera cam = client.getCamera();
		
		playersWeaponNode.setLocalTranslation(cam.getLocation()); //playersWeaponNode.getWorldTranslation();
		playersWeaponNode.lookAt(cam.getLocation().add(cam.getDirection()), Vector3f.UNIT_Y);
		
		if (currentlyReloading) {
			float gunRotSpeed = 400;
			float diff = (gunRotSpeed * tpfSecs);

			this.finishedReloadAt -= tpfSecs;
			if (finishedReloadAt > 0) {
				if (gunAngle < 90) {
					gunAngle += diff;
					weaponModel.rotate((float)Math.toRadians(-diff), 0f, 0f);
				}
			} else {
				if (gunAngle > 0) {
					gunAngle -= diff;
					weaponModel.rotate((float)Math.toRadians(diff), 0f, 0f);
				} else {
					currentlyReloading = false;
				}
			}
			if (Globals.DEBUG_GUN_ROTATION) {
				Globals.p("Gun angle = " + gunAngle);
			}
		}
	}
	

	@Override
	public void startReloading(float durationSecs) {
		this.finishedReloadAt = durationSecs;
		this.currentlyReloading = true;
		
	}


	@Override
	public void show(Node node) {
		if (playersWeaponNode.getParent() == null) {
			node.attachChild(playersWeaponNode);
		}
		
	}


	@Override
	public void hide() {
		playersWeaponNode.removeFromParent();		
	}


	@Override
	public Vector3f getPOVBulletStartPos() {
		return weaponModel.getWorldTranslation();
	}
	
}
