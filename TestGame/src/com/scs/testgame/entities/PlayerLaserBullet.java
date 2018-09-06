package com.scs.testgame.entities;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.entities.AbstractBullet;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.models.BeamLaserModel;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.testgame.TestGameClientEntityCreator;

public class PlayerLaserBullet extends AbstractBullet implements INotifiedOfCollision {

	public static final float RANGE = 30f;
	public static final float SPEED = 10f;
	private static final boolean USE_CYLINDER = true;

	public PlayerLaserBullet(IEntityController _game, int id, int playerId, IEntity _shooter, Vector3f startPos, Vector3f _dir, byte _side, ClientData _client) {
		super(_game, id, TestGameClientEntityCreator.LASER_BULLET, "LaserBullet", playerId, _shooter, startPos, _dir, _side, _client, true, SPEED, RANGE);

		//this.getMainNode().setUserData(Globals.ENTITY, this);

	}


	@Override
	protected void createModelAndSimpleRigidBody(Vector3f dir) {
		Spatial laserNode = null;
		if (USE_CYLINDER) {
			Vector3f origin = Vector3f.ZERO;
			laserNode = BeamLaserModel.Factory(game.getAssetManager(), origin, origin.add(dir.mult(.2f)), ColorRGBA.Pink, !game.isServer(), "Textures/cells3.png", 0.02f, Globals.BULLETS_CONES);
		} else {
			Mesh sphere = null;
			sphere = new Sphere(8, 8, 0.02f, true, false);
			laserNode = new Geometry("DebuggingSphere", sphere);

			TextureKey key3 = null;
			key3 = new TextureKey( "Textures/sun.jpg");
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			Material floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");
			floor_mat.setTexture("DiffuseMap", tex3);
			laserNode.setMaterial(floor_mat);
		}

		//laserNode.setShadowMode(ShadowMode.Cast);
		this.mainNode.attachChild(laserNode);

	}


	@Override
	public float getDamageCaused() {
		float dist = Math.max(1, this.getDistanceTravelled());
		return ((RANGE-dist) / dist) * 1;
	}


	@Override
	public void collided(PhysicalEntity pe) {
		//this.remove();
		game.markForRemoval(this.getID());
	}

}
