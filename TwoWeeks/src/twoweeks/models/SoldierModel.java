package twoweeks.models;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.scs.stevetech1.components.IAvatarModel;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.jme.JMEModelFunctions;
import com.scs.stevetech1.server.Globals;

public class SoldierModel implements IAvatarModel {

	public static final float MODEL_HEIGHT = 0.7f;
	private static final float MODEL_WIDTH = 0.3f;
	private static final float MODEL_DEPTH = 0.3f;

	private AssetManager assetManager;
	private Spatial model;
	private AnimChannel channel;
	public boolean isJumping = false;
	private int currAnimCode = -1;

	public SoldierModel(AssetManager _assetManager) {
		assetManager = _assetManager;
	}


	@Override
	public Spatial createAndGetModel(int side) {
		if (!Globals.USE_BOXES_FOR_AVATARS_SOLDIER) {
			model = assetManager.loadModel("Models/AnimatedHuman/Animated Human.blend");
			//if (side == 1) { todo - random tex
				JMEModelFunctions.setTextureOnSpatial(assetManager, model, "Models/AnimatedHuman/Textures/side1.png");
			/*} else if (side == 2) {
				JMEModelFunctions.setTextureOnSpatial(assetManager, model, "Models/AnimatedHuman/Textures/side2.png");
			} else {
				throw new RuntimeException("Unknown side: " + side);
			}*/
			JMEModelFunctions.scaleModelToHeight(model, MODEL_HEIGHT);
			JMEModelFunctions.moveYOriginTo(model, 0f);

			AnimControl control = JMEModelFunctions.getNodeWithControls((Node)model);
			//control.addListener(l);
			channel = control.createChannel();
		} else {
			Box box1 = new Box(MODEL_WIDTH/2, MODEL_HEIGHT/2, MODEL_DEPTH/2);
			model = new Geometry("Soldier", box1);
			model.setLocalTranslation(0, MODEL_HEIGHT/2, 0); // Move origin to floor
			JMEModelFunctions.setTextureOnSpatial(assetManager, model, "Textures/greensun.jpg");
		}
		model.setShadowMode(ShadowMode.CastAndReceive);
		return model;
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
		if (currAnimCode == animCode) {
			return;			
		}

		if (this.isJumping && animCode != AbstractAvatar.ANIM_DIED) {
			// Do nothing; only dying can stop a jumping anim
			return;
		}

		switch (animCode) {
		case AbstractAvatar.ANIM_DIED:
			channel.setAnim("Death");
			channel.setLoopMode(LoopMode.DontLoop);
			break;

		case AbstractAvatar.ANIM_IDLE:
			channel.setLoopMode(LoopMode.Loop);
			channel.setAnim("Idle");
			break;

		case AbstractAvatar.ANIM_WALKING:
			channel.setLoopMode(LoopMode.Loop);
			channel.setAnim("Walk"); // Was "Run"
			break;

		case AbstractAvatar.ANIM_SHOOTING:
			channel.setLoopMode(LoopMode.DontLoop);
			channel.setAnim("Punch");
			break;

		case AbstractAvatar.ANIM_JUMP:
			channel.setLoopMode(LoopMode.DontLoop);
			channel.setAnim("Jump");
			isJumping = true;
			break;

		default:
			Globals.pe(this.getClass().getSimpleName() + ": Unable to show anim " + animCode);
		}

		currAnimCode = animCode;
	}


	public int getCurrentAnimCode() {
		return this.currAnimCode;
	}


	@Override
	public BoundingBox getBoundingBox() {
		return new BoundingBox(new Vector3f(), MODEL_WIDTH/2, MODEL_HEIGHT/2, MODEL_DEPTH/2);
	}


	@Override
	public Spatial getModel() {
		return model;
	}

}
