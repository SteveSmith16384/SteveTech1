package com.scs.stetech1.weapons;

import com.jme3.math.Vector3f;
import com.scs.stetech1.components.ICalcHitInPast;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.entities.DebuggingSphere;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.RayCollisionData;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.IEntityController;

public class HitscanRifle extends AbstractMagazineGun implements ICalcHitInPast {
	
	private static final float RANGE = 30f;
	
	public RayCollisionData hitThisMoment = null; // Only used server-side

	public HitscanRifle(IEntityController game, int num, ICanShoot _shooter) {
		super(game, num, "Hitscan Rifle", _shooter, .2f, 1f, 10);
	}
	

	@Override
	public void launchBullet() {
		if (game.isServer()) {
			// We have already calculated the hit as part of ICalcHitInPast
			if (hitThisMoment != null) {
				Settings.p(hitThisMoment + " shot!");
				Vector3f pos = this.hitThisMoment.point;
				new DebuggingSphere(game, AbstractGameServer.getNextEntityID(), pos.x, pos.y, pos.z);
				// todo
				
				this.hitThisMoment = null; // Clear it ready for next loop
			}
		} else {
			// todo - nozzle flash or something
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


}
