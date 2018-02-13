package com.scs.testgame.entities;

import java.util.HashMap;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IClientControlled;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.components.ILaunchable;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.components.IRemoveOnContact;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.models.BeamLaserModel;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.testgame.TestGameClientEntityCreator;

public class LaserBullet extends PhysicalEntity implements IProcessByClient, ICausesHarmOnContact, ILaunchable, IRemoveOnContact, IClientControlled {

	private float timeLeft = 3f;

	//private ICorrectClientEntityPosition syncPos;
	//public PositionCalculator clientAvatarPositionData = new PositionCalculator(true, 500); // So we know where we were in the past to compare against where the server says we should have been
	private boolean launched = false;
	public IEntity shooter; // So we know who not to collide with
	private int side;

	public LaserBullet(IEntityController _game, int id, IEntityContainer<LaserBullet> owner, int _side) {
		super(_game, id, TestGameClientEntityCreator.LASER_BULLET, "LaserBullet", true);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("side", _side);
			creationData.put("containerID", owner.getID());
		}

		if (owner != null) { // Only snowball fired by us have an owner
			owner.addToCache(this);
		}

		side = _side;
		

		//game.addEntity(this);
		
		//syncPos = new InstantPositionAdjustment();

		this.collideable = false;
	}


	public void launch(IEntity _shooter, Vector3f startPos, Vector3f dir) {
		if (launched) { // We might be the client that fired the bullet, we we've already launched
			Globals.p("LaserBullet already launched.  This may be a good sign.");
			return;
		}
		
		if (_shooter == null) {
			throw new RuntimeException("Null launcher");
		}
		
		
		launched = true;
		shooter = _shooter;

		// Create the model now since we know the direction
		Vector3f origin = new Vector3f();// shooter.getBulletStartPos().clone();//getWorldTranslation().clone();
		Node laserNode = BeamLaserModel.Factory(game.getAssetManager(), origin, origin.add(dir.mult(1)), ColorRGBA.Pink, !game.isServer());
		this.mainNode.attachChild(laserNode);

		this.getMainNode().setUserData(Globals.ENTITY, this);
		laserNode.setUserData(Globals.ENTITY, this);

		launched = true;

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), true, this);
		simpleRigidBody.setAerodynamicness(1);
		simpleRigidBody.setGravity(0);

		game.getGameNode().attachChild(this.mainNode);
		this.setWorldTranslation(startPos);
		this.simpleRigidBody.setLinearVelocity(dir.normalize().mult(20));
		this.collideable = true;

	}

	
	@Override
	public float getDamageCaused() {
		return 10;
	}


	@Override
	public int getSide() {
		return side;
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		if (launched) {
			this.timeLeft -= tpf_secs;
			if (this.timeLeft < 0) {
				this.remove();
			}
			super.processByServer(server, tpf_secs);
		}
	}


	@Override
	public void processByClient(AbstractGameClient client, float tpf_secs) {
		if (launched) {
			simpleRigidBody.process(tpf_secs);

			this.timeLeft -= tpf_secs;
			if (this.timeLeft < 0) {
				this.remove();
			}
		}
		//super.processByServer(null, tpf_secs);

	}


	@Override
	public boolean isClientControlled() {
		return launched; // All launched bullets are under client control
	}


	@Override
	public IEntity getLauncher() {
		return shooter;
	}


	@Override
	public boolean hasBeenLaunched() {
		return this.hasBeenLaunched();
	}


	@Override
	public IEntity getActualShooter() {
		return this.shooter;
	}

}
