package com.scs.testgame.entities;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.JmeContext;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.components.ICausesHarmOnContact;
import com.scs.stetech1.components.IRemoveOnContact;
import com.scs.stetech1.entities.PhysicalEntity;
import com.scs.stetech1.models.BeamLaserModel;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityTypes;
import com.scs.stetech1.shared.IEntityController;

public class LaserBullet extends PhysicalEntity implements ICausesHarmOnContact, IRemoveOnContact {

	public ICanShoot shooter;
	private float timeLeft = 3;

	public LaserBullet(IEntityController _game, int id, ICanShoot _shooter, float x, float y, float z) {
		super(_game, id, EntityTypes.LASER_BULLET, "LaserBullet");

		this.shooter = _shooter;

		Vector3f origin = shooter.getBulletStartPos().clone();//getWorldTranslation().clone();
		//origin.addLocal(shooter.getBulletStartOffset());

		Node laserNode = BeamLaserModel.Factory(game.getAssetManager(), origin, origin.add(shooter.getShootDir().multLocal(1)), ColorRGBA.Pink, game.getJmeContext() != JmeContext.Type.Headless);

		this.mainNode.attachChild(laserNode);
		game.getRootNode().attachChild(this.mainNode);
		//laserNode.setLocalTranslation(shooter.getWorldTranslation().add(shooter.getShootDir().multLocal(AbstractPlayersAvatar.PLAYER_RAD*3)));
		//laserNode.getLocalTranslation().y -= 0.1f; // Drop bullets slightly
		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), this);
		if (_game.isServer()) {
			// Accelerate the physical ball to shoot it.
			simpleRigidBody.setLinearVelocity(shooter.getShootDir().mult(30));
			simpleRigidBody.setAerodynamicness(1);
			simpleRigidBody.setGravity(0);
		}
		
		this.getMainNode().setUserData(Settings.ENTITY, this);
		laserNode.setUserData(Settings.ENTITY, this);
		game.addEntity(this);

	}

/*
	@Override
	public boolean hasMoved() {
		return false; // We don't want to send updates to the client since it's impossible to keep them in sync
	}
	*/
	
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


	@Override
	public float getDamageCaused() {
		return 10;
	}


	@Override
	public int getSide() {
		return shooter.getSide();
	}


}
