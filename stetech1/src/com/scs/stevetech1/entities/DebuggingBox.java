package com.scs.stevetech1.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class DebuggingBox extends PhysicalEntity implements IProcessByClient {
	
	private static final float DURATION = 10;
	
	private float timeLeft = DURATION;

	public DebuggingBox(IEntityController _game, int type, int id, float x, float y, float z, float w, float h, float d) {
		super(_game, id, type, "DebuggingBox", true);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("size", new Vector3f(w, h, d));
		}
		
		this.collideable = false;
		
		Mesh sphere = new Box(w, h, d);
		Geometry ball_geo = new Geometry("DebuggingBox", sphere);

		TextureKey key3 = new TextureKey( "Textures/greensun.jpg");
		Texture tex3 = game.getAssetManager().loadTexture(key3);
		Material floor_mat = null;
			floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");
			floor_mat.setTexture("DiffuseMap", tex3);
		ball_geo.setMaterial(floor_mat);

		this.mainNode.attachChild(ball_geo);
		this.mainNode.setLocalTranslation(x, y, z);
		this.getMainNode().setUserData(Globals.ENTITY, this);
	}

/*
	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		if (game.isServer()) {
			this.timeLeft -= tpf_secs;
			if (this.timeLeft <= 0) {
				this.remove();
			}
		}
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
