package com.scs.undercoverzombie.models;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
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

public class HouseModel extends PhysicalEntity {

	private static final float w = 3.8f;
	private static final float h = 3f;
	private static final float d = 3f;
	
	public HouseModel(IEntityController _game, int id, float x, float y, float z, float rotDegrees) {
		super(_game, id, TestGameClientEntityCreator.HOUSE, "House", false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("rot", rotDegrees);
		}

		Spatial model = null;
		if (!_game.isServer()) { // !_game.isServer()) { // Not running in server
			model = game.getAssetManager().loadModel("Models/3d-character/environment/house/model.blend");
			model.scale(0.4f);
			model.setModelBound(new BoundingBox());
			model.setLocalTranslation(0, .15f, 0); // todo - why do I need this?
		} else {
			Box box1 = new Box(w/2, h/2, d/2);
			model = new Geometry(this.getName(), box1);
			model.setLocalTranslation(0, h/2, 0);
		}
		this.mainNode.attachChild(model); //This creates the model bounds!
		float rads = (float)Math.toRadians(rotDegrees);
		mainNode.rotate(0, rads, 0);
		mainNode.setLocalTranslation(x, y, z);
		game.getRootNode().attachChild(this.mainNode);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), false, this);
		this.simpleRigidBody.setMovable(false);

		model.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

		game.addEntity(this);

	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf) {
		super.processByServer(server, tpf);  //mainNode.getChild(0).getWorldTranslation();
	}

}
