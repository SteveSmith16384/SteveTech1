package com.scs.moonbaseassault.models;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.scs.moonbaseassault.entities.SoldierEnemyAvatar;
import com.scs.stevetech1.components.IAvatarModel;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.jme.JMEFunctions;
import com.scs.stevetech1.server.Globals;

//Punch, Walk, Working, ArmatureAction.002, Idle, Death, Run, Jump
public class SoldierModel implements IAvatarModel {

	private static final float MODEL_WIDTH = 0.25f;
	private static final float MODEL_DEPTH = 0.25f;
	private static final float MODEL_HEIGHT = 0.7f;

	private AssetManager assetManager;
	private Spatial model;
	private AnimChannel channel;
	private AnimEventListener l;
	public boolean isJumping = false;

	public SoldierModel(AssetManager _assetManager) {
		assetManager = _assetManager;
	}


	@Override
	public Spatial createAndGetModel(boolean forClient, int side) {
		//if (forClient && Globals.USE_SERVER_MODELS_ON_CLIENT == false) {
			model = assetManager.loadModel("Models/AnimatedHuman/Animated Human.blend");
			if (side == 1) {
				JMEFunctions.setTextureOnSpatial(assetManager, model, "Models/AnimatedHuman/Textures/side1.png");
			} else if (side == 2) {
				JMEFunctions.setTextureOnSpatial(assetManager, model, "Models/AnimatedHuman/Textures/side2.png");
			}
			JMEFunctions.scaleModelToHeight(model, MODEL_HEIGHT);
			JMEFunctions.moveYOriginTo(model, 0f);

			AnimControl control = JMEFunctions.getNodeWithControls((Node)model);
			control.addListener(l);
			channel = control.createChannel();

			return model;
		/*} else {
			Box box1 = new Box(MODEL_WIDTH/2, MODEL_HEIGHT/2, MODEL_DEPTH/2);
			model = new Geometry("Soldier", box1);
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


	public void setAnim(int animCode) {
		if (this.isJumping && animCode != AbstractAvatar.ANIM_DIED) {
			// Do nothing; only dying can stop a jumping anim
			return;
		}
		
		switch (animCode) {
		case AbstractAvatar.ANIM_DIED:
			channel.setAnim("Death");
			break;
			
		case AbstractAvatar.ANIM_IDLE:
			channel.setAnim("Idle");
			break;
			
		case AbstractAvatar.ANIM_WALKING:
			channel.setAnim("Run");
			break;
			
		case AbstractAvatar.ANIM_SHOOTING:
			channel.setAnim("Punch");
			break;
			
		case AbstractAvatar.ANIM_JUMP:
			channel.setAnim("Jump");
			isJumping = true;
			break;

		default:
			Globals.pe(this.getClass().getSimpleName() + ": Unable to show anim " + animCode);
		}
	}


}
