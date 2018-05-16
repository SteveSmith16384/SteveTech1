package com.scs.moonbaseassault.entities;

import java.util.HashMap;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Spatial;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.stevetech1.components.IGetRotation;
import com.scs.stevetech1.components.ISetRotation;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.jme.JMEModelFunctions;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class FlyingSpaceship2 extends PhysicalEntity implements ISetRotation, IGetRotation {

	private static final float TURN_SPEED = 0.2f;
	private static final float FWD_SPEED = 0.1f;

	public FlyingSpaceship2(IEntityController _game, int id, float x, float y, float z) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.FLYING_SPACESHIP2, "FlyingSpaceship2", true, false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
		}

		//Spatial model = game.getAssetManager().loadModel("Models/spaceships/Spaceship2.blend");
		//Spatial model = game.getAssetManager().loadModel("Models/spaceships2/BigSpaceship.obj");
		Spatial model = game.getAssetManager().loadModel("Models/spaceships2/SpaceShip.obj");
		JMEModelFunctions.moveYOriginTo(model, 0.1f);
		if (!_game.isServer()) {
			//JMEModelFunctions.setTextureOnSpatial(game.getAssetManager(), model, "Models/spaceships2/BigSpaceship.png");
			JMEModelFunctions.setTextureOnSpatial(game.getAssetManager(), model, "Models/spaceships2/SpaceshipTexture.png");
			model.setShadowMode(ShadowMode.CastAndReceive);
			JMEAngleFunctions.rotateToDirection(model, new Vector3f(1, 0, 0)); // Face the right way
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

		this.turnRight(tpf_secs);
		JMEAngleFunctions.moveForwards(this.getMainNode(), FWD_SPEED);
		
		this.sendUpdate = true;
		
		//Globals.p("FlyingSHip at " + this.getWorldTranslation());
	}


	public void turnLeft(float tpf) {
		this.getMainNode().rotate(new Quaternion().fromAngleAxis(-1 * TURN_SPEED * tpf, Vector3f.UNIT_Y));
	}


	public void turnRight(float tpf) {
		this.getMainNode().rotate(new Quaternion().fromAngleAxis(1 * TURN_SPEED * tpf, Vector3f.UNIT_Y));
	}


	@Override
	public Vector3f getRotation() {
		return this.getMainNode().getLocalRotation().getRotationColumn(2);
	}


	@Override
	public void setRotation(Vector3f dir) {
		JMEAngleFunctions.rotateToDirection(this.getMainNode(), dir);
		
	}



}
