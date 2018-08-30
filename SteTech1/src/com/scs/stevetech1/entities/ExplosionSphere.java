package com.scs.stevetech1.entities;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

import ssmith.lang.NumberFunctions;

public class ExplosionSphere extends PhysicalEntity implements IProcessByClient {

	private static final float SIZE_INC = 1.05f;
	
	private Geometry ball_geo;
	private float currentRad = 1;
	
	public ExplosionSphere(IEntityController _game, float x, float y, float z) {//, String tex) {
		super(_game, _game.getNextEntityID(), Globals.EXPLOSION_SPHERE, "ExplosionSphere", true, false, false);

		Sphere sphere = new Sphere(16, 16, currentRad, true, false);
		ball_geo = new Geometry("ExplosionSphere", sphere);
		//TextureKey key3 = new TextureKey( "Textures/sun.jpg");
		TextureKey key3 = new TextureKey( "Textures/roblox.png");
		Texture tex3 = game.getAssetManager().loadTexture(key3);
		Material floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");
		floor_mat.setTexture("DiffuseMap", tex3);
		ball_geo.setMaterial(floor_mat);

		this.mainNode.attachChild(ball_geo);
		this.mainNode.setLocalTranslation(x, y, z);
		
		ball_geo.setQueueBucket(Bucket.Transparent);
		floor_mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

		mainNode.setUserData(Globals.ENTITY, this);

	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		ball_geo.scale(SIZE_INC);
		if (ball_geo.getWorldScale().x > 5) {
			//this.remove();
			game.markForRemoval(this.getID());
		}
	}


}
