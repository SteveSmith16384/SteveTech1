package com.scs.moonbaseassault.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.entities.AbstractAIBullet;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.models.BeamLaserModel;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class AILaserBullet extends AbstractAIBullet implements INotifiedOfCollision {

	private static final boolean USE_CYLINDER = true;

	public AILaserBullet(IEntityController _game, int id, int side, float x, float y, float z, IEntity _shooter, Vector3f dir) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.AI_LASER_BULLET, x, y, z, "LaserBullet", side, _shooter, dir, true, 20);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("side", side);
			creationData.put("shooterID", shooter.getID());
			creationData.put("dir", dir);
		}

		this.getMainNode().setUserData(Globals.ENTITY, this);

	}


	@Override
	protected void createSimpleRigidBody(Vector3f dir) {
		Spatial laserNode = null;
		if (USE_CYLINDER) {
			Vector3f origin = Vector3f.ZERO;
			laserNode = BeamLaserModel.Factory(game.getAssetManager(), origin, origin.add(dir.mult(.2f)), ColorRGBA.Pink, !game.isServer(), "Textures/cells3.png", Globals.LASER_DIAM);
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

		/*No, since we use a Ray
		 * this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), game.isServer(), this);
		this.simpleRigidBody.setAerodynamicness(1);
		this.simpleRigidBody.setGravity(0);
		if (game.isServer()) {
			this.simpleRigidBody.setLinearVelocity(dir.normalize().mult(10)); // 20));  // Only the server moves the bullet
		}*/

	}


	@Override
	public float getDamageCaused() {
		return 1;
	}

/*
	@Override
	public void processByServer(AbstractEntityServer server, float tpf_secs) {
		super.processByServer(server, tpf_secs);
		
	}
*/

	@Override
	public void collided(PhysicalEntity pe) {
		if (game.isServer()) {
			if (!Globals.HIDE_EXPLOSION) {
				//todo ExplosionEffectEntity expl = new ExplosionEffectEntity(game, game.getNextEntityID(), this.getWorldTranslation());
				//game.addEntity(expl);
			}

		}
		this.remove();
	}

}
