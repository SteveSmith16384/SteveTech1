package com.scs.stetech1.weapons;

import com.scs.stetech1.components.ICalcHitInPast;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.IEntityController;
import com.scs.stetech1.shared.entities.PhysicalEntity;

public class HitscanRifle extends AbstractMagazineGun implements ICalcHitInPast {
	
	private static final float RANGE = 30f;
	
	public PhysicalEntity hitThisMoment; // Only used server-side

	public HitscanRifle(IEntityController _game, ICanShoot _shooter) {
		super(_game, "Hitscan Rifle", _shooter, 200, 500, 10);
	}

	
	@Override
	public void process(float interpol) {
		this.hitThisMoment = null;
		super.process(interpol);
		
	}
	
	
	@Override
	public void launchBullet() {//IEntityController _game, ICanShoot _shooter) {
		if (game.isServer()) {
			// We have already calculated the hit as part of ICalcHitInPast
			if (hitThisMoment != null) {
				Settings.p(hitThisMoment + " shot!");
				// todo
			}
			
			
		} else {
			// todo - nozzle flash or something
		}
		
	}


	@Override
	public void setTarget(PhysicalEntity pe) {
		this.hitThisMoment = pe;
		
	}


	@Override
	public float getRange() {
		return RANGE;
	}

}
