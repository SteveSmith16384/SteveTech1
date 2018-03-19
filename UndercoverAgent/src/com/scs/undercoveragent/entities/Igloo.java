package com.scs.undercoveragent.entities;

import java.util.HashMap;
import java.util.concurrent.Callable;

import com.jme3.math.Quaternion;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Spatial;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;

public class Igloo extends PhysicalEntity {

	public Igloo(IEntityController _game, int id, float x, float y, float z, Quaternion q) {
		super(_game, id, UndercoverAgentClientEntityCreator.IGLOO, "Igloo", true, false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			//creationData.put("q", q);
		}

		Spatial model = game.getAssetManager().loadModel("Models/Holiday/Igloo.blend");
		if (_game.isServer()) {
			model.setShadowMode(ShadowMode.CastAndReceive);
		}
		this.mainNode.attachChild(model); //This creates the model bounds!  mainNode.getWorldBound();

		mainNode.setLocalRotation(q);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), false, this);
		simpleRigidBody.setModelComplexity(3);

		model.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

	}
	
/*	
	private void loadInThread() {
		game.enqueue(new Callable<Spatial>() {
			public Spatial call() throws Exception {
				return node;
			}
		});

	}
*/

}
