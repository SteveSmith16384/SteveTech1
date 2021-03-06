package com.scs.stevetech1.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class DebuggingSphere extends PhysicalEntity implements IProcessByClient {

	private static final float DURATION = 5;

	private float timeLeft = DURATION;
	private boolean remove;

	public DebuggingSphere(IEntityController _game, int id, float x, float y, float z, boolean server, boolean _remove) {
		super(_game, id, Globals.DEBUGGING_SPHERE, "DebuggingSphere", true, false, false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
		}

		remove = _remove;
		this.collideable = false;

		Mesh sphere = null;
		if (server) {
			sphere = new Sphere(8, 8, .11f, true, false);
		} else {
			sphere = new Box(0.02f, 0.1f, 0.1f);
		}
		Geometry ball_geo = new Geometry("DebuggingSphere", sphere);
		ball_geo.setShadowMode(ShadowMode.CastAndReceive);
		TextureKey key3 = null;
		if (server) {
			key3 = new TextureKey( "Textures/sun.jpg");
		} else {
			key3 = new TextureKey( "Textures/greensun.jpg");
		}
		Texture tex3 = game.getAssetManager().loadTexture(key3);
		Material floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");
		floor_mat.setTexture("DiffuseMap", tex3);
		ball_geo.setMaterial(floor_mat);

		this.mainNode.attachChild(ball_geo);
		this.mainNode.setLocalTranslation(x, y, z);
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		if (remove) {
			this.timeLeft -= tpf_secs;
			if (this.timeLeft <= 0) {
				game.markForRemoval(this);
			}
		}
	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		if (remove) {
			this.timeLeft -= tpf_secs;
			if (this.timeLeft <= 0) {
				game.markForRemoval(this);
			}
		}
	}

}
