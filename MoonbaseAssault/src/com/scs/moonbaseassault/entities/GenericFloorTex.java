package com.scs.moonbaseassault.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class GenericFloorTex extends PhysicalEntity {

	public GenericFloorTex(IEntityController _game, int id, float x, float y, float z, float w, float d, String tex) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.FLOOR_TEX, "GenericFloorTex", false, true, false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("size", new Vector3f(w, 0, d));
			creationData.put("tex", tex);
			//creationData.put("name", name);
		}

		Quad q = new Quad(w, d);
		Geometry geometry = new Geometry("FloorGeom", q);
		if (!_game.isServer()) { // Not running in server
			geometry.setShadowMode(ShadowMode.Receive);

			TextureKey key3 = new TextureKey(tex);
			key3.setGenerateMips(true);
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			tex3.setWrap(WrapMode.Repeat);

			Material floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
			floor_mat.setTexture("DiffuseMap", tex3);
			geometry.setMaterial(floor_mat);
		}
		this.mainNode.attachChild(geometry);
		JMEAngleFunctions.rotateToDirection(geometry, new Vector3f(0, 1, 0));
		geometry.setLocalTranslation((w/2), 0.001f, (d/2));
		mainNode.setLocalTranslation(x, y, z);

		geometry.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);
	}

}
