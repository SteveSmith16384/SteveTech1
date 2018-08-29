package com.scs.stevetech1.entities;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

import ssmith.lang.NumberFunctions;

public class ExplosionShard extends PhysicalEntity implements IProcessByClient {

	private float timeLeft = 3f;
	private float rot = NumberFunctions.rndFloat(0,365);
	private Geometry geometry;


	public ExplosionShard(IEntityController _game, float x, float y, float z, float size, Vector3f forceDirection, String tex) {
		super(_game, _game.getNextEntityID(), Globals.EXPLOSION_SHARD, "ExplosionShard", true, false, true);

		Box box1 = new Box(size, size, size);
		geometry = new Geometry("ExplosionShard", box1);
		TextureKey key3 = new TextureKey(tex);//"Textures/sun.jpg");
		key3.setGenerateMips(true);
		Texture tex3 = game.getAssetManager().loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);

		geometry.setShadowMode(ShadowMode.CastAndReceive);

		Material floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
		floor_mat.setTexture("DiffuseMap", tex3);
		geometry.setMaterial(floor_mat);

		floor_mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha); 
		geometry.setQueueBucket(Bucket.Transparent);

		this.mainNode.attachChild(geometry);
		mainNode.setLocalTranslation(x, y, z);

		geometry.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), true, this);
		simpleRigidBody.setBounciness(0.5f);
		simpleRigidBody.setLinearVelocity(forceDirection);

	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		this.simpleRigidBody.process(tpf_secs);
		timeLeft -= tpf_secs;

		rot += tpf_secs;
		if (rot > 360) {
			rot = 0;
		}
		float radsX = (float)Math.toRadians(rot);
		geometry.rotate(radsX, -radsX, 0);


		if (timeLeft <= 0) {
			this.remove();
			if (Globals.STRICT) {
				if (this.getMainNode().getParent() != null) {
					Globals.pe("Warning: still have a parent!");
				}
			}
		}
	}


}
