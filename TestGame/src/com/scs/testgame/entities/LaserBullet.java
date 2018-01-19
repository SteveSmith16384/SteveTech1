package com.scs.testgame.entities;

import java.util.HashMap;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.client.HistoricalPositionCalculator;
import com.scs.stevetech1.client.syncposition.ICorrectClientEntityPosition;
import com.scs.stevetech1.client.syncposition.InstantPositionAdjustment;
import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.ILaunchable;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.components.IRemoveOnContact;
import com.scs.stevetech1.components.IRequiresAmmoCache;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.models.BeamLaserModel;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.shared.PositionCalculator;
import com.scs.testgame.TestGameClientEntityCreator;

public class LaserBullet extends PhysicalEntity implements IProcessByClient, ICausesHarmOnContact, ILaunchable, IRemoveOnContact {

	private float timeLeft = 3f;

	private ICorrectClientEntityPosition syncPos;
	public PositionCalculator clientAvatarPositionData = new PositionCalculator(true, 500); // So we know where we were in the past to compare against where the server says we should have been
	private boolean launched = false;
	public ICanShoot shooter; // So we know who not to collide with


	public LaserBullet(IEntityController _game, int id, IRequiresAmmoCache<LaserBullet> owner) {
		super(_game, id, TestGameClientEntityCreator.LASER_BULLET, "LaserBullet", true);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			//creationData.put("side", side);
			creationData.put("containerID", owner.getID());
		}

		owner.addToCache(this);

		game.addEntity(this);
		
		syncPos = new InstantPositionAdjustment();

		this.collideable = false;
	}


	public void launch(ICanShoot _shooter) {
		shooter = _shooter;

		// Create the model now since we know the direction
		Vector3f origin = new Vector3f();// shooter.getBulletStartPos().clone();//getWorldTranslation().clone();
		Node laserNode = BeamLaserModel.Factory(game.getAssetManager(), origin, origin.add(shooter.getShootDir().multLocal(1)), ColorRGBA.Pink, !game.isServer());
		this.mainNode.attachChild(laserNode);

		this.getMainNode().setUserData(Globals.ENTITY, this);
		laserNode.setUserData(Globals.ENTITY, this);

		launched = true;

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), true, this);
		simpleRigidBody.setAerodynamicness(1);
		simpleRigidBody.setGravity(0);

		game.getRootNode().attachChild(this.mainNode);
		this.setWorldTranslation(_shooter.getBulletStartPos());
		this.simpleRigidBody.setLinearVelocity(_shooter.getShootDir().normalize().mult(20));
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


	@Override
	public void calcPosition(AbstractGameClient mainApp, long serverTimeToUse) {
		if (launched) {
			if (Globals.SYNC_GRENADE_POS) {
				Vector3f offset = HistoricalPositionCalculator.calcHistoricalPositionOffset(serverPositionData, clientAvatarPositionData, serverTimeToUse, mainApp.pingRTT/2);
				if (offset != null) {
					this.syncPos.adjustPosition(this, offset);
				}
			}
		}
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
	public ICanShoot getLauncher() {
		return shooter;
	}



}
