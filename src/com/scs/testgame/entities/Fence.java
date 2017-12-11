package com.scs.testgame.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.JmeContext;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stetech1.entities.PhysicalEntity;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityTypes;
import com.scs.stetech1.shared.IEntityController;

public class Fence extends PhysicalEntity {

	private static final float WIDTH = 2f;

	public Fence(IEntityController _game, int id, float x, float height, float z, float rot, String tex) {
		super(_game, id, EntityTypes.FENCE, "Fence");

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			//creationData.put("id", id);
			//creationData.put("rot", rot);
			creationData.put("tex", tex);
		}

		Box box1 = new Box(WIDTH/2, height/2, .1f);
		box1.scaleTextureCoordinates(new Vector2f(WIDTH, height));
		Geometry geometry = new Geometry("Fence", box1);
		if (_game.getJmeContext() != JmeContext.Type.Headless) { // !_game.isServer()) { // Not running in server
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
			// Uncomment if tex is transparent
			//floor_mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
			//geometry.setQueueBucket(Bucket.Transparent);
		}
		this.mainNode.attachChild(geometry);
		float rads = (float)Math.toRadians(rot);
		mainNode.rotate(0, rads, 0);
		mainNode.setLocalTranslation(x+(WIDTH/2), height/2, z+0.5f);

			this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, (SimplePhysicsController)game, this);
			this.simpleRigidBody.setMovable(false);

		game.getRootNode().attachChild(this.mainNode);

		geometry.setUserData(Settings.ENTITY, this);

	}

/*
	@Override
	public void process(AbstractGameServer server, float tpf) {
		// Do nothing
	}
*/

}
