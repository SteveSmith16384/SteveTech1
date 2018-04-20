package com.scs.moonbaseassault.entities;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.entities.AbstractPlayersBullet;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.models.BeamLaserModel;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class PlayerLaserBullet extends AbstractPlayersBullet implements INotifiedOfCollision {

	private static final boolean USE_CYLINDER = true;

	public PlayerLaserBullet(IEntityController _game, int id, int playerOwnerId, IEntityContainer<AbstractPlayersBullet> owner, int _side, ClientData _client, Vector3f dir) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.PLAYER_LASER_BULLET, "LaserBullet", playerOwnerId, owner, _side, _client, dir, true, 10f, 30f);

		this.getMainNode().setUserData(Globals.ENTITY, this);

	}


	@Override
	protected void createSimpleRigidBody(Vector3f dir) {
		Spatial laserNode = null;
		if (USE_CYLINDER) {
			Vector3f origin = Vector3f.ZERO;
			laserNode = BeamLaserModel.Factory(game.getAssetManager(), origin, origin.add(dir.mult(.2f)), ColorRGBA.Pink, !game.isServer(), "Textures/cells3.png");
		} else {
			Mesh sphere = null;
			sphere = new Sphere(8, 8, 0.02f, true, false);
			laserNode = new Geometry("DebuggingSphere", sphere);

			TextureKey key3 = null;
			key3 = new TextureKey( "Textures/sun.jpg");
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			Material floor_mat = null;
				floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");
				floor_mat.setTexture("DiffuseMap", tex3);
			laserNode.setMaterial(floor_mat);
		}

		//laserNode.setShadowMode(ShadowMode.Cast);
		this.mainNode.attachChild(laserNode);

		/*
		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), true, this);
		simpleRigidBody.setAerodynamicness(1);
		simpleRigidBody.setGravity(0);
		this.simpleRigidBody.setLinearVelocity(dir.normalize().mult(10)); // 20));
*/
	}


	@Override
	public float getDamageCaused() {
		return 10;
	}

/*
	@Override
	public void processByServer(AbstractEntityServer server, float tpf_secs) {
		if (launched) {
			if (Globals.DEBUG_NO_BULLET) {
				//Globals.p("Shooter at " + ((PhysicalEntity)this.shooter).getWorldTranslation());
				Globals.p("Bullet at " + this.getWorldTranslation());
			}
			super.processByServer(server, tpf_secs);

		}
	}

/*
	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		if (launched) {
			if (Globals.DEBUG_NO_BULLET) {
				//Globals.p("Shooter at " + ((PhysicalEntity)this.shooter).getWorldTranslation());
				Globals.p("Bullet at " + this.getWorldTranslation());
			}
			//simpleRigidBody.process(tpf_secs);

			float dist = this.origin.distance(this.getWorldTranslation());
			if (dist > 30) {
				this.remove();
			}
		}
	}
*/

	@Override
	public void collided(PhysicalEntity pe) {
		if (game.isServer()) {
			/*if (!Globals.HIDE_EXPLOSION) {
				ExplosionEffectEntity expl = new ExplosionEffectEntity(game, game.getNextEntityID(), this.getWorldTranslation());
				game.addEntity(expl);
			}*/

		}
		this.remove();
	}

}