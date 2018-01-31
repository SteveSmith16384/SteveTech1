package com.scs.testgame.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.JmeContext;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IAffectedByPhysics;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.testgame.TestGameClientEntityCreator;

public class Crate extends PhysicalEntity implements IAffectedByPhysics {

	public Crate(IEntityController _game, int id, float x, float y, float z, float w, float h, float d, String tex, float rotDegrees) {
		super(_game, id, TestGameClientEntityCreator.CRATE, "Crate", true);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("size", new Vector3f(w, h, d));
			creationData.put("tex", tex);
			//creationData.put("rot", rotDegrees); No, since chances are it will have moved anyway
		}

		Box box1 = new Box(w/2, h/2, d/2);

		Geometry geometry = new Geometry("Crate", box1);
		if (!_game.isServer()) { // Not running in server
			TextureKey key3 = new TextureKey(tex);
			key3.setGenerateMips(true);
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			tex3.setWrap(WrapMode.Repeat);

			Material floor_mat = null;
			if (Globals.LIGHTING) {
				floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
				floor_mat.setTexture("DiffuseMap", tex3);
			} else {
				floor_mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
				floor_mat.setTexture("ColorMap", tex3);
			}

			geometry.setMaterial(floor_mat);
			//floor_mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
			//geometry.setQueueBucket(Bucket.Transparent);
		}
		geometry.setLocalTranslation(0, h/2, 0);
		this.mainNode.attachChild(geometry); //This creates the model bounds!  mainNode.getWorldBound();
		float rads = (float)Math.toRadians(rotDegrees);
		mainNode.rotate(0, rads, 0);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), true, this);

		geometry.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

		//game.getRootNode().attachChild(this.mainNode);
		//game.addEntity(this);

	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf) {
		super.processByServer(server, tpf);
	}

}
