package com.scs.testgame.entities;

import java.util.HashMap;

import com.jme3.bounding.BoundingBox;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.testgame.TestGameClientEntityCreator;

public class House extends PhysicalEntity {

	private static final float w = 3.8f;
	private static final float h = 3f;
	private static final float d = 3f;
	
	public House(IEntityController _game, int id, float x, float y, float z, float rotDegrees) {
		super(_game, id, TestGameClientEntityCreator.HOUSE, "House", false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("rot", rotDegrees);
		}

		Spatial model = null;
		if (!_game.isServer()) {
			model = game.getAssetManager().loadModel("Models/3d-character/environment/house/model.blend");
			model.scale(0.4f);
			model.setModelBound(new BoundingBox());
			//model.setLocalTranslation(0, .15f, 0);
		} else {
			Box box1 = new Box(w/2, h/2, d/2);
			model = new Geometry(this.getName(), box1);
			model.setLocalTranslation(0, h/2, 0);
		}
		this.mainNode.attachChild(model); //This creates the model bounds!
		float rads = (float)Math.toRadians(rotDegrees);
		mainNode.rotate(0, rads, 0);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), false, this);
		//this.simpleRigidBody.setMovable(false);

		model.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

		//game.getRootNode().attachChild(this.mainNode);
		//game.addEntity(this);

	}


	@Override
	public void processByServer(AbstractEntityServer server, float tpf) {
		super.processByServer(server, tpf);  //mainNode.getChild(0).getWorldTranslation();
	}

}
