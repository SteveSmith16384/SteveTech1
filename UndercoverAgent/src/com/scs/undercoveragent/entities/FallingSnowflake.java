package com.scs.undercoveragent.entities;

import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;

/**
 * A scenery entity that is client-side only
 * @author stephencs
 *
 */
public class FallingSnowflake extends PhysicalEntity implements IProcessByClient, INotifiedOfCollision {

	private static final float RAD = 0.1f;

	public FallingSnowflake(IEntityController _game, int id, Vector3f pos) {
		super(_game, id, UndercoverAgentClientEntityCreator.FALLING_SNOWFLAKE, "Falling Snowflake", true, false, true);

		Sphere sphere = new Sphere(8, 8, RAD, true, false);
		sphere.setTextureMode(TextureMode.Projected);
		Geometry ball_geo = new Geometry("snowball_geom", sphere);

		if (game.isServer()) {
			throw new RuntimeException("This should not be on the server!");
		}

		ball_geo.setShadowMode(ShadowMode.Cast);
		TextureKey key3 = new TextureKey("Textures/snow.jpg");
		Texture tex3 = game.getAssetManager().loadTexture(key3);
		Material mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");
		mat.setTexture("DiffuseMap", tex3);
		ball_geo.setMaterial(mat);

		ball_geo.setModelBound(new BoundingBox());
		this.mainNode.attachChild(ball_geo);
		this.mainNode.setLocalTranslation(pos);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), true, this);
		this.simpleRigidBody.setBounciness(0f);
		this.simpleRigidBody.setSolid(false);
	}


	@Override
	public void notifiedOfCollision(PhysicalEntity pe) {
		game.markForRemoval(this);
	}


	@Override
	public void processByClient(IClientApp client, float tpfSecs) {
		this.getWorldTranslation();
		this.simpleRigidBody.process(tpfSecs);
		if (this.getWorldTranslation().y < 0) {
			game.markForRemoval(this);
		}

	}
}
