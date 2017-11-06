package com.scs.stetech1.weapons;

import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.shared.IEntityController;

public class HitscanRifle extends AbstractMagazineGun {

	public HitscanRifle(IEntityController _game, ICanShoot _shooter) {
		super(_game, "Hitscan Rifle", _shooter, 200, 500, 10);
	}

	
	@Override
	public void launchBullet() {//IEntityController _game, ICanShoot _shooter) {
		if (game.isServer()) {
			// We have already rewound the avatars
			// TODO Check for a hit!
			
			
		} else {
			// todo - nozzle flash or something
		}
		
	}

}
