package com.scs.testgame.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.testgame.TestGameClientEntityCreator;

/*
 * Simple sphere to help show points in the world
 */
public class DebuggingSphere extends PhysicalEntity implements IProcessByClient {
	
	private static final float DURATION = 5;
	
	private float timeLeft = DURATION;

	public DebuggingSphere(IEntityController _game, int id, float x, float y, float z, boolean server) {
		super(_game, id, TestGameClientEntityCreator.DEBUGGING_SPHERE, "DebuggingSphere", true);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
		}
		
		this.collideable = false;
		
		Mesh sphere = null;
		if (server) {
			sphere = new Sphere(8, 8, 0.2f, true, false);
		} else {
			sphere = new Box(0.2f, 0.2f, 0.2f);
		}
		//sphere.setTextureMode(TextureMode.Projected);
		Geometry ball_geo = new Geometry("DebuggingSphere", sphere);

		TextureKey key3 = null;
		if (server) {
			key3 = new TextureKey( "Textures/sun.jpg");
		} else {
			key3 = new TextureKey( "Textures/greensun.jpg");
		}
		Texture tex3 = game.getAssetManager().loadTexture(key3);
		Material floor_mat = null;
			floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");
			floor_mat.setTexture("DiffuseMap", tex3);
		ball_geo.setMaterial(floor_mat);

		this.mainNode.attachChild(ball_geo);
		this.mainNode.setLocalTranslation(x, y, z);
		//ball_geo.setLocalTranslation(shooter.getWorldTranslation().add(shooter.getShootDir().multLocal(AbstractPlayersAvatar.PLAYER_RAD*2)));
		
		//this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), this);
		//this.simpleRigidBody.setMovable(false);

		this.getMainNode().setUserData(Globals.ENTITY, this);
		//game.getRootNode().attachChild(this.mainNode);
		//game.addEntity(this);

	}


	@Override
	public void processByServer(AbstractEntityServer server, float tpf_secs) {
		if (game.isServer()) {
			this.timeLeft -= tpf_secs;
			if (this.timeLeft <= 0) {
				this.remove();
			}
		}
	}

/*
	@Override
	public boolean canMove() {
		return false;
	}
*/

	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		if (this.getID() <= 0) { // Client-controlled
			this.timeLeft -= tpf_secs;
			if (this.timeLeft <= 0) {
				this.remove();
			}

		}
		
	}


}
