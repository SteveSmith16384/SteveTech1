package com.scs.undercoveragent.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;

/*
 * 
 * The origin for this should be left/bottom/front.
 *
 */
public class InvisibleMapBorder extends PhysicalEntity {

	public static final float BORDER_WIDTH = 2f;
	public static final float BORDER_HEIGHT = 5f;

	public InvisibleMapBorder(IEntityController _game, int id, float x, float y, float z, float size, Vector3f dir) {
		super(_game, id, UndercoverAgentClientEntityCreator.INVISIBLE_MAP_BORDER, "InvisibleMapBorder", false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("size", size);
			creationData.put("dir", dir);
		}

		Box box1 = new Box(BORDER_WIDTH/2, BORDER_HEIGHT/2, size/2);
		Geometry geometry = new Geometry("MapBorderBox", box1);
		if (!_game.isServer()) { // Not running in server
			if (Globals.USE_SERVER_MODELS_ON_CLIENT) {
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
			} else {
				geometry.setCullHint(CullHint.Always); // DOn't draw the box on the client
			}
		}

		geometry.setLocalTranslation(-BORDER_WIDTH/2, BORDER_HEIGHT/2, size/2);
		mainNode.attachChild(geometry);
		JMEAngleFunctions.rotateToDirection(mainNode, dir);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), false, this);
		simpleRigidBody.setModelComplexity(0);

		geometry.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);
	}

}
