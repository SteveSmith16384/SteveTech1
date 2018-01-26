package com.scs.undercoveragent.entities;

import java.util.HashMap;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Quaternion;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.JMEFunctions;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;

public class MapBorder extends PhysicalEntity {
	
	private static final float MODEL_W_H = 4f;

	public MapBorder(IEntityController _game, int id, float x, float y, float z, float size, Quaternion q) {
		super(_game, id, UndercoverAgentClientEntityCreator.MAP_BORDER, "MapBorder", false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("size", size);
			creationData.put("q", q);
		}


		if (!game.isServer()) {
			for (float i=(MODEL_W_H/2) ; i<size ; i+=MODEL_W_H) {
				Spatial model = game.getAssetManager().loadModel("Models/Holiday/Terrain2.blend");
				JMEFunctions.scaleModelToWidth(model, MODEL_W_H);
				model.setLocalTranslation(MODEL_W_H/2, 0, i);
				this.mainNode.attachChild(model);
			}
			this.mainNode.setModelBound(new BoundingBox());
		} else {
			Box box1 = new Box(MODEL_W_H/2, 100/2, size/2);

		}

		mainNode.setLocalRotation(q);

		mainNode.setLocalTranslation(x, y, z);
		game.getRootNode().attachChild(this.mainNode);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), false, this);
		simpleRigidBody.setModelComplexity(0);

		//model.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

		game.addEntity(this);

	}

}
