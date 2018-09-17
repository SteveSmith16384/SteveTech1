package com.scs.undercoveragent.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.collision.Collidable;
import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.EntityPositionData;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;
import com.scs.undercoveragent.models.SnowmanModel;

/**
 * For testing rewinding
 * @author stephencs
 *
 */
public class MovingTargetSnowman extends PhysicalEntity implements IRewindable, IDamagable, IProcessByClient {

	private static final float DURATION = 3;
	private static final float SPEED = 5;

	private Vector3f currDir = new Vector3f(1f, 0, 0);
	private float timeUntilTurn = DURATION;
	private Spatial debugNode;

	public MovingTargetSnowman(IEntityController _game, int id, float x, float y, float z, Quaternion q) {
		super(_game, id, UndercoverAgentClientEntityCreator.MOVING_TARGET_SNOWMAN, "SnowmanMovingTarget", true, true, true);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
		}

		SnowmanModel m = new SnowmanModel(game.getAssetManager());
		Spatial model = m.createAndGetModel();
		this.mainNode.attachChild(model);

		mainNode.setLocalRotation(q);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), game.isServer(), this);
		simpleRigidBody.setModelComplexity(2);
		simpleRigidBody.setNeverMoves(false);

		model.setUserData(Globals.ENTITY, this);
		
		if (Globals.TEST_BULLET_REWINDING) {
			createDebugBox();
		}

	}


	private void createDebugBox() {
		Box box1 = new Box(SnowmanModel.MODEL_WIDTH/2, SnowmanModel.MODEL_HEIGHT/2, SnowmanModel.MODEL_WIDTH/2);
		debugNode = new Geometry("DebugBox", box1);
		TextureKey key3 = new TextureKey("Textures/neon1.jpg");
		key3.setGenerateMips(true);
		Texture tex3 = game.getAssetManager().loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);

		Material floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
		floor_mat.setTexture("DiffuseMap", tex3);
		debugNode.setMaterial(floor_mat);

		debugNode.setLocalTranslation(0, box1.yExtent/2, 0); // Origin is at the bottom

		game.getGameNode().attachChild(debugNode);

	}


	@Override
	public void processByServer(AbstractGameServer server, float tpfSecs) {
		this.timeUntilTurn -= tpfSecs;
		if (this.timeUntilTurn <= 0) {
			this.timeUntilTurn = DURATION;
			this.currDir.multLocal(-1);
		}

		this.simpleRigidBody.setAdditionalForce(this.currDir.mult(SPEED));

		super.processByServer(server, tpfSecs);
	}


	@Override
	public Collidable getCollidable() {
		return this.mainNode;
	}


	@Override
	public byte getSide() {
		return 0;
	}


	@Override
	public float getHealth() {
		return 0;
	}


	@Override
	public void updateClientSideHealth(int amt) {
		// Do nothing
		
	}
	
	
	@Override
	public void damaged(float amt, IEntity collider, String reason) {
		// Do nothing
	}


	@Override
	public void remove() {
		super.remove();
		if (Globals.TEST_BULLET_REWINDING) {
			this.debugNode.removeFromParent();
		}
	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		if (Globals.TEST_BULLET_REWINDING) {
			EntityPositionData epd = historicalPositionData.calcPosition(System.currentTimeMillis(), false);
			if (epd != null) {
				debugNode.setLocalTranslation(epd.position);
			}

		}
	}



	
}
