package com.scs.testgame.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.JmeContext;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stetech1.components.IAffectedByPhysics;
import com.scs.stetech1.components.IDamagable;
import com.scs.stetech1.components.IRewindable;
import com.scs.stetech1.entities.PhysicalEntity;
import com.scs.stetech1.netmessages.EntityUpdateMessage;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityTypes;
import com.scs.stetech1.shared.IEntityController;

public class MovingTarget extends PhysicalEntity implements IAffectedByPhysics, IRewindable, IDamagable {

	private static final float DURATION = 3;
	private static final float SPEED = 7;

	private Vector3f currDir = new Vector3f(1f, 0, 0);
	private float timeUntilTurn = DURATION;

	public MovingTarget(IEntityController _game, int id, float x, float y, float z, float w, float h, float d, String tex, float rotDegrees) {
		super(_game, id, EntityTypes.MOVING_TARGET, "MovingTarget");

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			//creationData.put("id", id);
			//creationData.put("pos", new Vector3f(x, y, z));
			creationData.put("size", new Vector3f(w, h, d));
			creationData.put("tex", tex);
			//creationData.put("rot", rotDegrees); No, since chances are it will have moved anyway
		}

		Box box1 = new Box(w/2, h/2, d/2);
		//box1.scaleTextureCoordinates(new Vector2f(WIDTH, HEIGHT));
		Geometry geometry = new Geometry("Crate", box1);
		if (_game.getJmeContext() != JmeContext.Type.Headless) {
			TextureKey key3 = new TextureKey(tex);
			key3.setGenerateMips(true);
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			tex3.setWrap(WrapMode.Repeat);

			Material floor_mat = null;
			if (Settings.LIGHTING) {
				floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
				floor_mat.setTexture("DiffuseMap", tex3);
			} else {
				floor_mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
				floor_mat.setTexture("ColorMap", tex3);
			}

			geometry.setMaterial(floor_mat);
		}
		geometry.setLocalTranslation(0, h/2, 0); // Origin is at the bottom
		this.mainNode.attachChild(geometry);
		float rads = (float)Math.toRadians(rotDegrees);
		mainNode.rotate(0, rads, 0);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), this);

		game.getRootNode().attachChild(this.mainNode);

		geometry.setUserData(Settings.ENTITY, this);
		mainNode.setUserData(Settings.ENTITY, this);

		game.addEntity(this);

	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		this.timeUntilTurn -= tpf_secs;
		if (this.timeUntilTurn <= 0) {
			this.timeUntilTurn = DURATION;
			this.currDir.multLocal(-1);
		}

		this.simpleRigidBody.setAdditionalForce(this.currDir.mult(SPEED)); // this.getMainNode();

		super.processByServer(server, tpf_secs);

	}


	@Override
	public void fallenOffEdge() {
		this.respawn();
	}


	private void respawn() {
		this.setWorldTranslation(new Vector3f(10, 10, 10));

		EntityUpdateMessage eum = new EntityUpdateMessage();
		eum.addEntityData(this, true);
		AbstractGameServer server = (AbstractGameServer)this.game;
		server.networkServer.sendMessageToAll(eum);

	}
	
	
	@Override
	public void damaged(float amt, String reason) {
		this.respawn();
	}


	@Override
	public int getSide() {
		// TODO Auto-generated method stub
		return 0;
	}


}
