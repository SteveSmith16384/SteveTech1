package com.scs.testgame.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IAffectedByPhysics;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.netmessages.EntityUpdateMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.testgame.TestGameClientEntityCreator;

import ssmith.io.IOFunctions;

public class MovingTarget extends PhysicalEntity implements IAffectedByPhysics, IRewindable, IDamagable {

	private static final float DURATION = 3;
	private static final float SPEED = 7;

	private Vector3f currDir = new Vector3f(1f, 0, 0);
	private float timeUntilTurn = DURATION;

	public MovingTarget(IEntityController _game, int id, float x, float y, float z, float w, float h, float d, String tex, float rotDegrees) {
		super(_game, id, TestGameClientEntityCreator.MOVING_TARGET, "MovingTarget", true, false, true);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("size", new Vector3f(w, h, d));
			creationData.put("tex", tex);
		}

		Box box1 = new Box(w/2, h/2, d/2);
		Geometry geometry = new Geometry("MovingTarget", box1);
		if (!_game.isServer()) { // Not running in server
			TextureKey key3 = new TextureKey(tex);
			key3.setGenerateMips(true);
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			tex3.setWrap(WrapMode.Repeat);

			Material mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
			mat.setTexture("DiffuseMap", tex3);
			geometry.setMaterial(mat);
		}
		geometry.setLocalTranslation(0, h, 0); // Origin is at the bottom
		this.mainNode.attachChild(geometry);
		float rads = (float)Math.toRadians(rotDegrees);
		mainNode.rotate(0, rads, 0);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), true, this);

		geometry.setUserData(Globals.ENTITY, this);
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

		if (Globals.LOG_MOVING_TARGET_POS) {
			IOFunctions.appendToFile("ServerMovingtarget.csv", "ServerMovingTarget," + System.currentTimeMillis() + "," + this.getWorldTranslation());
		}

	}


	@Override
	public void fallenOffEdge() {
		this.respawn();
	}


	private void respawn() {
		this.setWorldTranslation(new Vector3f(10, 10, 10));

		EntityUpdateMessage eum = new EntityUpdateMessage();
		eum.addEntityData(this, true, this.createEntityUpdateDataRecord());
		AbstractGameServer server = (AbstractGameServer)this.game;
		server.sendMessageToInGameClients(eum);

	}


	@Override
	public void damaged(float amt, IEntity collider, String reason) {
		// Do nothing
	}


	@Override
	public byte getSide() {
		return 0;
	}


	@Override
	public void calcPosition(long serverTimeToUse, float tpf_secs) {
		super.calcPosition(serverTimeToUse, tpf_secs);

		if (Globals.LOG_MOVING_TARGET_POS) {
			IOFunctions.appendToFile("ClientMovingtarget.csv", "ClientMovingTarget," + serverTimeToUse + "," + this.getWorldTranslation());
		}

	}


	@Override
	public float getHealth() {
		return 1;
	}


	@Override
	public void updateClientSideHealth(int amt) {
		// Do nothing
		
	}
	

	@Override
	public boolean canBeDamaged() {
		return true;
	}


}
