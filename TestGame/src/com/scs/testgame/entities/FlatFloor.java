package com.scs.testgame.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.testgame.TestGameClientEntityCreator;

public class FlatFloor extends PhysicalEntity {

	public FlatFloor(IEntityController _game, int id, float x, float y, float z, float w, float d, String tex) {
		super(_game, id, TestGameClientEntityCreator.FLAT_FLOOR, "FlatFloor", false, false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			//creationData.put("pos", new Vector3f(x, yTop, z));
			creationData.put("size", new Vector3f(w, 0, d));
			creationData.put("tex", tex);
		}


		Quad q = new Quad(w, d);

		Geometry geometry = new Geometry("FloorGeom", q);
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
		}
		this.mainNode.attachChild(geometry);
		geometry.setLocalTranslation((w/2), 0, (d/2)); // Move it into position
		geometry.setLocalRotation(new Quaternion(0, 0, 1, 1)); // wrong?
		mainNode.setLocalTranslation(x, y, z); // Move it into position

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), false, this);
		//this.simpleRigidBody.setMovable(false);


		geometry.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

		//game.getRootNode().attachChild(this.mainNode);
		//game.addEntity(this);

	}



	@Override
	public void processByServer(AbstractEntityServer server, float tpf_secs) {
		// Do nothing

	}


}
