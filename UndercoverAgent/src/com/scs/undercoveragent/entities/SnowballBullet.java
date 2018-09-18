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
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.entities.AbstractBullet;
import com.scs.stevetech1.entities.DebuggingSphere;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.UASounds;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;

public class SnowballBullet extends AbstractBullet implements INotifiedOfCollision {

	private static final float RAD = 0.1f;

	public SnowballBullet(IEntityController _game, int id, int playerOwnerId, IEntity _shooter, Vector3f startPos, Vector3f _dir, byte _side, ClientData _client) {
		super(_game, id, UndercoverAgentClientEntityCreator.SNOWBALL_BULLET, "Snowball", playerOwnerId, _shooter, startPos, _dir, _side, _client, false, 0f, 0f);
	}


	@Override
	public float getDamageCaused() {
		return 1;
	}


	@Override
	protected void createModelAndSimpleRigidBody(Vector3f dir) {
		Sphere sphere = new Sphere(8, 8, RAD, true, false);
		sphere.setTextureMode(TextureMode.Projected);
		Geometry ball_geo = new Geometry("snowball_geom", sphere);

		if (!game.isServer()) {
			ball_geo.setShadowMode(ShadowMode.Cast);
			TextureKey key3 = new TextureKey("Textures/snow.jpg");
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			Material mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");
			mat.setTexture("DiffuseMap", tex3);
			ball_geo.setMaterial(mat);
		}

		ball_geo.setModelBound(new BoundingBox());
		this.mainNode.attachChild(ball_geo);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), true, this);
		this.simpleRigidBody.setBounciness(0f);
		this.simpleRigidBody.setLinearVelocity(dir.normalize().mult(20));

		if (!game.isServer()) {
			game.playSound(UASounds.THROW, this.getID(), this.origin, Globals.DEFAULT_VOLUME, false);
		}

	}


	@Override
	public void notifiedOfCollision(PhysicalEntity pe) {
		if (game.isServer()) {
			if (Globals.SHOW_BULLET_COLLISION_POS) {
				// Create debugging sphere
				Vector3f pos = this.getWorldTranslation();
				DebuggingSphere ds = new DebuggingSphere(game, game.getNextEntityID(), pos.x, pos.y, pos.z, true, false);
				game.addEntity(ds);
			}
			//if (Globals.TEST_BULLET_REWINDING) {
			Globals.p("Server: " + this.getName() + " has hit " + pe);
			//}
		} else {
			game.playSound(UASounds.SMALL_SPLAT, -1, this.getWorldTranslation(), Globals.DEFAULT_VOLUME, false);
		}
		game.markForRemoval(this);
	}


}
