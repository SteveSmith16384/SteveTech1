package com.scs.moonbaseassault.entities;

import java.util.HashMap;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Spatial;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.jme.JMEModelFunctions;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class FlyingSpaceship2 extends PhysicalEntity {

	//private Vector3f currDir;

	public FlyingSpaceship2(IEntityController _game, int id, float x, float y, float z) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.FLYING_SPACESHIP2, "FlyingSpaceship2", true, false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
		}

		Spatial model = game.getAssetManager().loadModel("Models/spaceships/Spaceship2.blend");
		JMEModelFunctions.moveYOriginTo(model, 0.1f);
		if (!_game.isServer()) {
			model.setShadowMode(ShadowMode.CastAndReceive);
		}
		this.mainNode.attachChild(model);
		//mainNode.setLocalRotation(q);
		mainNode.setLocalTranslation(x, y, z);

		model.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

	}

	
	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		super.processByServer(server, tpf_secs);
		
		Vector3f dirOld = this.getWorldRotation().getRotationColumn(2);
		JMEAngleFunctions.rotateBy(this.getMainNode(), 1);
		//Vector3f dir0 = this.getWorldRotation().getRotationColumn(0);
		//Vector3f dir1 = this.getWorldRotation().getRotationColumn(1);
		Vector3f dir2 = this.getWorldRotation().getRotationColumn(2);
		this.adjustWorldTranslation(dir2.mult(tpf_secs));
		this.getMainNode().lookAt(this.getWorldTranslation().add(dir2), Vector3f.UNIT_Y); // Point us in the right direction
		
		
	}

	
}
