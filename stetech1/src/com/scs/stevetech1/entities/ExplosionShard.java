package com.scs.stevetech1.entities;

import com.jme3.asset.TextureKey;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.sun.scenario.Settings;

import ssmith.lang.NumberFunctions;

public class ExplosionShard extends PhysicalEntity implements IProcessByClient {

	public static void Factory(IEntityController _game, int id, int type, Vector3f pos, int num) {
		for (int i=0 ; i<num ; i++) {
			ExplosionShard s = new ExplosionShard(_game, id, type, pos.x, pos.y, pos.z);
			_game.getGameNode().attachChild(s.getMainNode());

		}
	}


	private float timeLeft = 8f; 

	private ExplosionShard(IEntityController _game, int id, int type, float x, float y, float z) {
		super(_game, id, type, "CubeExplosionShard", true, false);

		float s = .1f;
		Box box1 = new Box(s, s, s);
		Geometry geometry = new Geometry("Crate", box1);
		TextureKey key3 = new TextureKey("Textures/sun.jpg");
		key3.setGenerateMips(true);
		Texture tex3 = game.getAssetManager().loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);

		Material floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
		floor_mat.setTexture("DiffuseMap", tex3);
		geometry.setMaterial(floor_mat);
		//floor_mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		//geometry.setQueueBucket(Bucket.Transparent);

		this.mainNode.attachChild(geometry);
		int rotDegreesX = NumberFunctions.rnd(0,365);
		float radsX = (float)Math.toRadians(rotDegreesX);
		int rotDegreesY = NumberFunctions.rnd(0,365);
		float radsY = (float)Math.toRadians(rotDegreesY);
		mainNode.rotate(radsX, radsY, 0);
		mainNode.setLocalTranslation(x, y, z);

		geometry.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), game.isServer(), this);
		Vector3f force = new Vector3f(NumberFunctions.rndFloat(-1, 1), NumberFunctions.rndFloat(1, 2), NumberFunctions.rndFloat(-1, 1));
		//Vector3f force = new Vector3f(0, 1.4f, 0);
		simpleRigidBody.setAdditionalForce(force);

	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
	//Settings.p("Pos: " + this.getLocation());
	timeLeft -= tpf_secs;
	if (timeLeft <= 0) {
		this.remove();
	}
}


}
