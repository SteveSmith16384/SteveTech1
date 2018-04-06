package com.scs.moonbaseassault.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

/*
 * 
 * The origin for this should be left/bottom/front.
 *
 */
public class MapBorder extends PhysicalEntity {

	public static final float BORDER_WIDTH = 2f;
	public static final float BORDER_HEIGHT = 5f;

	public MapBorder(IEntityController _game, int id, float x, float y, float z, float size, Vector3f dir) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.MAP_BORDER, "InvisibleMapBorder", false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("size", size);
			creationData.put("dir", dir);
		}

		Box box1 = new Box(BORDER_WIDTH/2, BORDER_HEIGHT/2, size/2);
		box1.scaleTextureCoordinates(new Vector2f(BORDER_WIDTH, BORDER_HEIGHT));
		Geometry geometry = new Geometry("MapBorderBox", box1);
		if (!_game.isServer()) { // Not running in server
			TextureKey key3 = new TextureKey("Textures/spacewall.png");
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

		geometry.setLocalTranslation(-BORDER_WIDTH/2, BORDER_HEIGHT/2, size/2);
		mainNode.attachChild(geometry);
		JMEAngleFunctions.rotateToDirection(mainNode, dir);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), false, this);
		simpleRigidBody.setModelComplexity(0);
		simpleRigidBody.setNeverMoves(true);

		geometry.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);
	}


}
