package com.scs.moonbaseassault.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.server.MoonbaseAssaultServer;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class Computer extends PhysicalEntity implements IDamagable {

	private static final float SIZE = 0.9f;
	private float health = 100;
	private MoonbaseAssaultServer server;
	
	public Computer(IEntityController _game, int id, float x, float y, float z) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.COMPUTER, "Computer", false, true);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
		}

		float w = SIZE;
		float h = SIZE;
		float d = SIZE;
		
		Box box1 = new Box(w/2, h/2, d/2);

		Geometry geometry = new Geometry("Computer", box1);
		if (!_game.isServer()) {
			geometry.setShadowMode(ShadowMode.CastAndReceive);
			
			TextureKey key3 = new TextureKey("Textures/computerconsole2.jpg");
			key3.setGenerateMips(true);
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			tex3.setWrap(WrapMode.Repeat);

			Material floor_mat = null;
				floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
				floor_mat.setTexture("DiffuseMap", tex3);

			geometry.setMaterial(floor_mat);
		} else {
			server = (MoonbaseAssaultServer)game;
		}
		this.mainNode.attachChild(geometry);
		geometry.setLocalTranslation(w/2, h/2, d/2);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), false, this);
		simpleRigidBody.setNeverMoves(true);

		geometry.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);
	}


	@Override
	public void damaged(float amt, ICausesHarmOnContact collider, String reason) {
		Globals.p("Computer hit!");
		this.health -= amt;
		if (this.health <= 0) {
			server.computerDestroyed();
			
			this.remove();
			
			Vector3f pos = this.getWorldTranslation();
			DestroyedComputer dc = new DestroyedComputer(game, game.getNextEntityID(), pos.x, pos.y, pos.z);
			game.addEntity(dc);
		}
			
	}


	@Override
	public int getSide() {
		return 2;
	}


	@Override
	public float getHealth() {
		return health;
	}

}
