package com.scs.moonbaseassault.entities;

import java.util.HashMap;

import com.jme3.collision.Collidable;
import com.jme3.math.Quaternion;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Spatial;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.JMEModelFunctions;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class Spaceship1 extends PhysicalEntity {

	public Spaceship1(IEntityController _game, int id, float x, float y, float z, Quaternion q) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.SPACESHIP1, "Spaceship1", false, true, false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
		}

		//Spatial model = game.getAssetManager().loadModel("Models/spaceships/Spaceship.blend");
		//Spatial model = game.getAssetManager().loadModel("Models/spaceships2/CroissantShip.obj");
		//JMEModelFunctions.setTextureOnSpatial(game.getAssetManager(), model, "Models/spaceships2/CroissantShipTexture.png");
		Spatial model = game.getAssetManager().loadModel("Models/spaceships2/Small Spaceship.obj");
		JMEModelFunctions.setTextureOnSpatial(game.getAssetManager(), model, "Models/spaceships2/SmallSpaceshipTexture.png");
		model.setLocalScale(.7f);
		JMEModelFunctions.moveYOriginTo(model, 0.1f);
		if (!_game.isServer()) {
			model.setShadowMode(ShadowMode.Cast);
		}
		this.mainNode.attachChild(model);
		mainNode.setLocalRotation(q);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), false, this);
		simpleRigidBody.setModelComplexity(3);

		model.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

	}


	@Override
	public Collidable getCollidable() {
		return this.mainNode; // Since it's a complex model
	}


}
