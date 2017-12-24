package com.scs.testgame.entities;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.JmeContext;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IRemoveOnContact;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.models.BeamLaserModel;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.testgame.TestGameEntityCreator;

public class LaserBullet extends PhysicalEntity implements ICausesHarmOnContact, IRemoveOnContact {

	public ICanShoot shooter;
	private float timeLeft = 3;

	public LaserBullet(IEntityController _game, int id, ICanShoot _shooter, float x, float y, float z) {
		super(_game, id, TestGameEntityCreator.LASER_BULLET, "LaserBullet");

		this.shooter = _shooter;

		Vector3f origin = shooter.getBulletStartPos().clone();//getWorldTranslation().clone();
		//origin.addLocal(shooter.getBulletStartOffset());

		Node laserNode = BeamLaserModel.Factory(game.getAssetManager(), origin, origin.add(shooter.getShootDir().multLocal(1)), ColorRGBA.Pink, game.getJmeContext() != JmeContext.Type.Headless);

		this.mainNode.attachChild(laserNode);
		//game.getRootNode().attachChild(this.mainNode);
		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), this);
			simpleRigidBody.setAerodynamicness(1);
			simpleRigidBody.setGravity(0);
		
		this.getMainNode().setUserData(Globals.ENTITY, this);
		laserNode.setUserData(Globals.ENTITY, this);
		game.addEntity(this);
		
		this.collideable = false;


	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		if (game.isServer()) {
			this.timeLeft -= tpf_secs;
			if (this.timeLeft < 0) {
				this.remove();
			}
		}
		super.processByServer(server, tpf_secs);
	}


	public void launch() {
		game.getRootNode().attachChild(this.mainNode);
		this.setWorldTranslation(this.shooter.getBulletStartPos());
		// Accelerate the physical ball to shoot it.
		simpleRigidBody.setLinearVelocity(shooter.getShootDir().mult(30));
		this.collideable = true;
	}

	
	@Override
	public float getDamageCaused() {
		return 10;
	}


	@Override
	public int getSide() {
		return shooter.getSide();
	}


}
