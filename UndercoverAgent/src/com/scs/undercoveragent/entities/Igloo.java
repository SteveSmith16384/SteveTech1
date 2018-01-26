package com.scs.undercoveragent.entities;

import java.util.HashMap;

import com.jme3.math.Quaternion;
import com.jme3.scene.Spatial;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;

public class Igloo extends PhysicalEntity {

	public Igloo(IEntityController _game, int id, float x, float y, float z, Quaternion q) {
		super(_game, id, UndercoverAgentClientEntityCreator.IGLOO, "Igloo", true);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("q", q);
		}

		Spatial model = game.getAssetManager().loadModel("Models/Holiday/Igloo.blend");
		this.mainNode.attachChild(model); //This creates the model bounds!  mainNode.getWorldBound();

		mainNode.setLocalRotation(q);
		mainNode.setLocalTranslation(x, y, z);
		game.getRootNode().attachChild(this.mainNode);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), false, this);
		simpleRigidBody.setModelComplexity(3);

		model.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

		game.addEntity(this);

	}

}
