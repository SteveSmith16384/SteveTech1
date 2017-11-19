package com.scs.stetech1.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import com.scs.stetech1.server.ServerMain;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityTypes;
import com.scs.stetech1.shared.IEntityController;

/*
 * Simple sphere to help show points in the world
 */
public class DebuggingSphere extends PhysicalEntity {

	public DebuggingSphere(IEntityController _game, int id, float x, float y, float z) {
		super(_game, id, EntityTypes.DEBUGGING_SPHERE, "DebuggingSphere");

		/*if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("id", id);
		}*/
		
		Sphere sphere = new Sphere(8, 8, 0.2f, true, false);
		sphere.setTextureMode(TextureMode.Projected);
		Geometry ball_geo = new Geometry("cannon ball", sphere);

		TextureKey key3 = new TextureKey( "Textures/sun.jpg");
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

		this.main_node.attachChild(ball_geo);
		this.main_node.setLocalTranslation(x, y, z);
		game.getRootNode().attachChild(this.main_node);
		//ball_geo.setLocalTranslation(shooter.getWorldTranslation().add(shooter.getShootDir().multLocal(AbstractPlayersAvatar.PLAYER_RAD*2)));
		
		/*rigidBodyControl = new RigidBodyControl(0f);
		ball_geo.addControl(rigidBodyControl);
		game.getBulletAppState().getPhysicsSpace().add(rigidBodyControl);
*/
		this.getMainNode().setUserData(Settings.ENTITY, this);
		//rigidBodyControl.setUserObject(this);
		game.addEntity(this);

	}


	@Override
	public void process(ServerMain server, float tpf) {

	}

}
