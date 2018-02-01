package com.scs.undercoveragent.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.JMEFunctions;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;

/*
 * The origin for this should be left/bottom/front
 *
 */
public class MapBorder extends PhysicalEntity {

	private static final float BORDER_WIDTH = 2f;
	private static final float BORDER_HEIGHT = 5f;

	public MapBorder(IEntityController _game, int id, float x, float y, float z, float size, Quaternion q) {
		super(_game, id, UndercoverAgentClientEntityCreator.MAP_BORDER, "MapBorder", false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("size", size);
			creationData.put("q", q);
		}

		/*todo if (!game.isServer()) {
			for (float i=(MODEL_W_H/2) ; i<size ; i+=MODEL_W_H) {
				Spatial model = game.getAssetManager().loadModel("Models/Holiday/Terrain2.blend");
				JMEFunctions.scaleModelToWidth(model, MODEL_W_H);
				model.setLocalTranslation(MODEL_W_H/2, 0, i);
				this.mainNode.attachChild(model);
			}
			this.mainNode.setModelBound(new BoundingBox());
		} else {*/
		Box box1 = new Box(BORDER_WIDTH/2, BORDER_HEIGHT/2, size/2);
		Geometry geometry = new Geometry("Crate", box1);
		if (!_game.isServer()) { // Not running in server
			TextureKey key3 = new TextureKey("Textures/neon1.jpg");
			//TextureKey key3 = new TextureKey("Textures/snow.jpg");
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
		//}
		geometry.setLocalTranslation(-BORDER_WIDTH/2, BORDER_HEIGHT/2, size/2);

		mainNode.attachChild(geometry);
		mainNode.setLocalRotation(q);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), false, this);
		simpleRigidBody.setModelComplexity(0);

		geometry.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

		//game.getRootNode().attachChild(this.mainNode);
		//game.addEntity(this);

	}

}
