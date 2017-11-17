package com.scs.stetech1.weapons;

import com.jme3.math.Vector3f;
import com.scs.stetech1.components.ICalcHitInPast;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.server.ServerMain;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.HitData;
import com.scs.stetech1.shared.IEntityController;
import com.scs.stetech1.shared.entities.DebuggingSphere;
import com.scs.stetech1.shared.entities.PhysicalEntity;

public class HitscanRifle extends AbstractMagazineGun implements ICalcHitInPast {
	
	private static final float RANGE = 30f;
	
	public HitData hitThisMoment = null; // Only used server-side

	public HitscanRifle(IEntityController game, ICanShoot _shooter) {
		super(game, "Hitscan Rifle", _shooter, 200, 500, 10);
	}

	
	/*@Override
	public void process(ServerMain server, float interpol) {
		// todo ! this.hitThisMoment = null;
		super.process(server, interpol);
		
	}*/
	
	
	@Override
	public void launchBullet() {
		if (game.isServer()) {
			// We have already calculated the hit as part of ICalcHitInPast
			if (hitThisMoment != null) {
				Settings.p(hitThisMoment + " shot!");
				Vector3f pos = this.hitThisMoment.pos;//.getWorldTranslation();
				new DebuggingSphere(game, ServerMain.getNextEntityID(), pos.x, pos.y, pos.z); 
				// todo
				
				this.hitThisMoment = null; // Clear it ready for next loop
			}
		} else {
			// todo - nozzle flash or something
		}
		
	}


	@Override
	public void setTarget(HitData hd) {
		this.hitThisMoment = hd;
		
	}


	@Override
	public float getRange() {
		return RANGE;
	}


}