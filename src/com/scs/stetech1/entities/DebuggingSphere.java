package com.scs.stetech1.entities;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityTypes;
import com.scs.stetech1.shared.IEntityController;

/*
 * Simple sphere to help show points in the world
 */
public class DebuggingSphere extends PhysicalEntity {
	
	private static final float DURATION = 10;
	
	private float timeLeft = DURATION;

	public DebuggingSphere(IEntityController _game, int id, float x, float y, float z, boolean server) {
		super(_game, id, EntityTypes.DEBUGGING_SPHERE, "DebuggingSphere");

		/*if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("id", id);
		}*/
		
		this.collideable = false;
		
		Sphere sphere = new Sphere(8, 8, 0.2f, true, false);
		sphere.setTextureMode(TextureMode.Projected);
		Geometry ball_geo = new Geometry("DebuggingSphere", sphere);

		TextureKey key3 = null;
		if (server) {
			key3 = new TextureKey( "Textures/sun.jpg");
		} else {
			key3 = new TextureKey( "Textures/greensun.jpg");
		}
		Texture tex3 = game.getAssetManager().loadTexture(key3);
		Material floor_mat = null;
		if (Settings.LIGHTING) {
			floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");
			floor_mat.setTexture("DiffuseMap", tex3);
		} else {
			floor_mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
			floor_mat.setTexture("ColorMap", tex3);
		}
		ball_geo.setMaterial(floor_mat);

		this.mainNode.attachChild(ball_geo);
		this.mainNode.setLocalTranslation(x, y, z);
		game.getRootNode().attachChild(this.mainNode);
		//ball_geo.setLocalTranslation(shooter.getWorldTranslation().add(shooter.getShootDir().multLocal(AbstractPlayersAvatar.PLAYER_RAD*2)));
		
		//this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), this);
		//this.simpleRigidBody.setMovable(false);

		this.getMainNode().setUserData(Settings.ENTITY, this);
		game.addEntity(this);

	}


	@Override
	public void process(AbstractGameServer server, float tpf) {
		if (game.isServer()) {
			this.timeLeft -= tpf;
			if (this.timeLeft <= 0) {
				this.remove();
			}
		}
	}


	@Override
	public boolean canMove() {
		return false;
	}


}
