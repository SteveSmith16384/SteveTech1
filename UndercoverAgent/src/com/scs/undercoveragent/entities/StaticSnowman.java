package com.scs.undercoveragent.entities;

import java.util.HashMap;

import com.jme3.collision.Collidable;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.scene.Spatial;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;
import com.scs.undercoveragent.models.SnowmanModel;

public class StaticSnowman extends PhysicalEntity {

	public StaticSnowman(IEntityController _game, int id, float x, float y, float z, Quaternion q) {
		super(_game, id, UndercoverAgentClientEntityCreator.STATIC_SNOWMAN, "Snowman", false, true, false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
		}

		SnowmanModel m = new SnowmanModel(game.getAssetManager());
		Spatial model = m.createAndGetModel();
		this.mainNode.attachChild(model);

		mainNode.setLocalRotation(q);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), false, this);
		simpleRigidBody.setModelComplexity(2);
		simpleRigidBody.setNeverMoves(true);

		model.setUserData(Globals.ENTITY, this);
/*
		if (Globals.TEST_OUTLINE_SHADER) {
			if (!_game.isServer()) {
				AbstractGameClient client = (AbstractGameClient)game;
				client.showOutlineEffect(model, 4, ColorRGBA.Red);
				model.updateModelBound();
				model.forceRefresh(true, false, true);
				model.updateGeometricState();
			}
		}
		*/
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf) {
		super.processByServer(server, tpf);
	}


	@Override
	public Collidable getCollidable() {
		return this.mainNode;
	}


}
