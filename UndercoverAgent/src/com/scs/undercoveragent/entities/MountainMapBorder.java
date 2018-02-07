package com.scs.undercoveragent.entities;

import java.util.HashMap;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.JMEFunctions;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;

public class MountainMapBorder extends PhysicalEntity {

	public MountainMapBorder(IEntityController _game, int id, float x, float y, float z, float size, Vector3f dir) {
		super(_game, id, UndercoverAgentClientEntityCreator.MOUNTAIN_MAP_BORDER, "MountainMapBorder", false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("size", size);
			creationData.put("dir", dir);
		}

		if (!_game.isServer()) { // Not running in server
			Node container = new Node("MountainContainer");
			// Add mountain models
			for (float i=(InvisibleMapBorder.BORDER_WIDTH/2) ; i<size ; i+=InvisibleMapBorder.BORDER_WIDTH) {
				Spatial model = game.getAssetManager().loadModel("Models/Holiday/Terrain2.blend");
				JMEFunctions.scaleModelToWidth(model, InvisibleMapBorder.BORDER_WIDTH);
				model.setLocalTranslation(InvisibleMapBorder.BORDER_WIDTH/2, 0, i);
				container.attachChild(model);
			}
			container.setModelBound(new BoundingBox());
			//container.setLocalTranslation(-InvisibleMapBorder.BORDER_WIDTH/2, InvisibleMapBorder.BORDER_HEIGHT/2, size/2);

			mainNode.attachChild(container);
			JMEFunctions.RotateToDirection(mainNode, dir);

			//this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), false, this);  NO Collision!
			//simpleRigidBody.setModelComplexity(0);

			//geometry.setUserData(Globals.ENTITY, this);
			//mainNode.setUserData(Globals.ENTITY, this);
		} else {
			// Do nothing on server
		}
		mainNode.setLocalTranslation(x, y, z);

	}

}
