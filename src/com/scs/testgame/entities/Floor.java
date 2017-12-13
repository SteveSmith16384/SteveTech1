package com.scs.testgame.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.shape.Box;
import com.jme3.system.JmeContext;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.BufferUtils;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stetech1.client.AbstractGameClient;
import com.scs.stetech1.components.IProcessByClient;
import com.scs.stetech1.entities.PhysicalEntity;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityTypes;
import com.scs.stetech1.shared.IEntityController;

public class Floor extends PhysicalEntity implements IProcessByClient {

	private Box box1;
	private Vector3f texScroll, thisScroll;
	private float w, h, d;

	public Floor(IEntityController _game, int id, float x, float yTop, float z, float w, float h, float d, String tex, Vector3f _texScroll) {
		super(_game, id, EntityTypes.FLOOR, "Floor");

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			//creationData.put("pos", new Vector3f(x, yTop, z));
			creationData.put("size", new Vector3f(w, h, d));
			creationData.put("tex", tex);
		}

		this.w = w;
		this.h = h;
		this.d = d;

		this.texScroll = _texScroll;
		thisScroll = new Vector3f();

		box1 = new Box(w/2, h/2, d/2);

		box1.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(new float[]{
				0, h, w, h, w, 0, 0, 0, // back
				0, h, d, h, d, 0, 0, 0, // right
				0, h, w, h, w, 0, 0, 0, // front
				0, h, d, h, d, 0, 0, 0, // left
				w, 0, w, d, 0, d, 0, 0, // top
				w, 0, w, d, 0, d, 0, 0  // bottom
		}));

		Geometry geometry = new Geometry("FloorGeom", box1);
		if (_game.getJmeContext() != JmeContext.Type.Headless) { // Not running in server
			TextureKey key3 = new TextureKey(tex);
			key3.setGenerateMips(true);
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			tex3.setWrap(WrapMode.Repeat);

			Material floor_mat = null;
			if (Settings.LIGHTING) {
				floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
				floor_mat.setTexture("DiffuseMap", tex3);
			} else {
				floor_mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
				floor_mat.setTexture("ColorMap", tex3);
			}
			geometry.setMaterial(floor_mat);
		}
		this.mainNode.attachChild(geometry);
		geometry.setLocalTranslation(x+(w/2), yTop-(h/2), z+(d/2)); // Move it into position

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), this);
		this.simpleRigidBody.setMovable(false);

		game.getRootNode().attachChild(this.mainNode);

		geometry.setUserData(Settings.ENTITY, this);
		mainNode.setUserData(Settings.ENTITY, this);

		game.addEntity(this);

	}


	@Override
	public void processByClient(AbstractGameClient client, float tpf) {
		if (texScroll != null) {
			float diff = tpf*1f;
			thisScroll.addLocal(diff, diff, diff);
			thisScroll.multLocal(this.texScroll);

			while (this.thisScroll.x > 1) {
				this.thisScroll.x--;
			}

			while (this.thisScroll.y > 1) {
				this.thisScroll.y--;
			}

			while (this.thisScroll.z > 1) {
				this.thisScroll.z--;
			}

			float offx = this.thisScroll.x;
			float offy = this.thisScroll.y;
			float offz = this.thisScroll.z;

			//Settings.p("thisScroll=" + thisScroll);

			box1.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(new float[]{
					offx, h+offy, w+offx, h+offy, w+offx, offy, offx, offy, // back
					offz, h+offy, d+offz, h+offy, d+offz, offy, offz, offy, // right
					offx, h+offy, w+offx, h+offy, w+offx, offy, offx, offy, // front
					offz, h+offy, d+offz, h+offy, d+offz, offy, offz, offy, // left
					w+offx, offz, w+offx, d+offz, offx, d+offz, offx, offz, // top
					w+offx, offz, w+offx, d+offz, offx, d+offz, offx, offz  // bottom
			}));

		}
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		// Do nothing

	}


	@Override
	public boolean canMove() {
		return false;
	}


}
