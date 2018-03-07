package com.scs.moonbaseassault.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class Computer extends PhysicalEntity implements IDamagable {

	private static final float SIZE = 0.9f;
	private float health = 1;
	
	public Computer(IEntityController _game, int id, float x, float y, float z, String tex) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.COMPUTER, "Computer", false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("tex", tex);
		}

		float w = SIZE;
		float h = SIZE;
		float d = SIZE;
		
		Box box1 = new Box(w/2, h/2, d/2);

		Geometry geometry = new Geometry("Crate", box1);
		if (!_game.isServer()) {
			TextureKey key3 = new TextureKey(tex);
			key3.setGenerateMips(true);
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			tex3.setWrap(WrapMode.Repeat);

			Material floor_mat = null;
			if (Globals.LIGHTING) {
				floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
				floor_mat.setTexture("DiffuseMap", tex3);
			} else {
				floor_mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
				floor_mat.setTexture("ColorMap", tex3);
			}

			geometry.setMaterial(floor_mat);
		}
		this.mainNode.attachChild(geometry);
		geometry.setLocalTranslation(w/2, h/2, d/2);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), false, this);

		geometry.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);
	}


	@Override
	public void damaged(float amt, ICausesHarmOnContact collider, String reason) {
		Globals.p("Computer hit!");
		this.health -= amt;
		if (this.health <= 0) {
			// todo - replace with damaged model
		}
			
	}


	@Override
	public int getSide() {
		return 2;
	}

}
