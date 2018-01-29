package com.scs.undercoveragent.entities;

import java.util.HashMap;

import com.jme3.math.Quaternion;
import com.jme3.scene.Spatial;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;
import com.scs.undercoveragent.models.SnowmanModel;

public class StaticSnowman extends PhysicalEntity {

	public StaticSnowman(IEntityController _game, int id, float x, float y, float z, Quaternion q) {
		super(_game, id, UndercoverAgentClientEntityCreator.STATIC_SNOWMAN, "Snowman", false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("q", q);
		}

		SnowmanModel m = new SnowmanModel(game.getAssetManager());
		Spatial model = m.getModel(!game.isServer());

		this.mainNode.attachChild(model); //This creates the model bounds!
		
		//float rads = (float)Math.toRadians(rotDegrees);
		//mainNode.rotate(0, rads, 0);
		mainNode.setLocalRotation(q);
		
		mainNode.setLocalTranslation(x, y, z);
		game.getRootNode().attachChild(this.mainNode);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), false, this);
		simpleRigidBody.setModelComplexity(2);

		model.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

		game.addEntity(this);

	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf) {
		super.processByServer(server, tpf);
	}

}