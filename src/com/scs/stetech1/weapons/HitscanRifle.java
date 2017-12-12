package com.scs.stetech1.weapons;

import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.scs.stetech1.components.ICalcHitInPast;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.components.ICausesHarmOnContact;
import com.scs.stetech1.entities.DebuggingSphere;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.RayCollisionData;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.IEntityController;

public class HitscanRifle extends AbstractMagazineGun implements ICalcHitInPast, ICausesHarmOnContact {

	private static final float RANGE = 99f;

	public RayCollisionData hitThisMoment = null; // Only used server-side

	public HitscanRifle(IEntityController game, int num, ICanShoot _shooter) {
		super(game, num, "Hitscan Rifle", _shooter, .2f, 1f, 10);
	}


	@Override
	public void launchBullet() {
		if (game.isServer()) {
			// We have already calculated the hit as part of ICalcHitInPast
			if (hitThisMoment != null) {
				Settings.p(hitThisMoment.entity + " has been shot!");
				Vector3f pos = this.hitThisMoment.point;
				new DebuggingSphere(game, AbstractGameServer.getNextEntityID(), pos.x, pos.y, pos.z, true);
				AbstractGameServer server = (AbstractGameServer)game;
				server.collisionLogic.collision(hitThisMoment.entity, this);
				this.hitThisMoment = null; // Clear it ready for next loop
			}
		} else {
			// todo - nozzle flash or something
			Vector3f from = shooter.getBulletStartPos();
			if (Settings.DEBUG_SHOOTING_POS) {
				Settings.p("Client shooting from " + from);
			}
			Ray ray = new Ray(from, shooter.getShootDir());
			RayCollisionData rcd = shooter.checkForCollisions(ray, RANGE);
			if (rcd != null) {
				Vector3f pos = rcd.point;
				new DebuggingSphere(game, -1, pos.x, pos.y, pos.z, false);
			}
		}

	}


	@Override
	public void setTarget(RayCollisionData hd) {
		this.hitThisMoment = hd;

	}


	@Override
	public float getRange() {
		return RANGE;
	}


	@Override
	public float getDamageCaused() {
		return 1;
	}


	@Override
	public int getSide() {
		return this.shooter.getSide();
	}

}
