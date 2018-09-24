package com.scs.testgame.entities;

import java.io.IOException;
import java.util.HashMap;

import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.Texture2D;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IAffectedByPhysics;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.AnimatedTexture;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.testgame.TestGameClientEntityCreator;

public class AnimatedWall extends PhysicalEntity implements IAffectedByPhysics, IProcessByClient {

	private AnimatedTexture animatedTex;
	private Texture tex3;
	
	public AnimatedWall(IEntityController _game, int id, float x, float yBottom, float z, float w, float h, float rotDegrees) {
		super(_game, id, TestGameClientEntityCreator.ANIMATED_WALL, "AnimatedWall", true, true, false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("w", w);
			creationData.put("h", h);
			//creationData.put("tex", tex);
			creationData.put("rot", rotDegrees);
		} else {
			this.animatedTex = new AnimatedTexture();
			try {
				this.animatedTex.loadImages("assets/Textures/ManicMiner2.gif");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		float d = 0.1f;

		Box box1 = new Box(w/2, h/2, d/2);
		Geometry geometry = new Geometry("Wall", box1);
		if (!_game.isServer()) { // Not running in server
			//TextureKey key3 = new TextureKey(tex);
			//key3.setGenerateMips(true);
			tex3 = new Texture2D(animatedTex.getNextImage());// game.getAssetManager().loadTexture(key3);
			tex3.setWrap(WrapMode.Repeat);
			Material mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
			mat.setTexture("DiffuseMap", tex3);
			geometry.setMaterial(mat);
		}
		this.mainNode.attachChild(geometry);
		if (rotDegrees != 0) {
			float rads = (float)Math.toRadians(rotDegrees);
			mainNode.rotate(0, rads, 0);
		}
		geometry.setLocalTranslation(w/2, h/2, d/2); // Never change position of mainNode (unless the whole object is moving)
		mainNode.setLocalTranslation(x, yBottom, z); // Never change position of mainNode (unless the whole object is moving)

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), false, this);
		simpleRigidBody.setNeverMoves(true);

		geometry.setUserData(Globals.ENTITY, this);
	}
	

	@Override
	public void processByClient(IClientApp client, float tpfSecs) {
		tex3.setImage(animatedTex.getNextImage());
	}


}
