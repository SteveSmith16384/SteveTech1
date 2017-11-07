package com.scs.stetech1.weapons;

import com.scs.stetech1.abilities.IAbility;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.shared.IEntityController;

/*
 * This gun shoots physical laser bolts
 */
public class LaserRifle extends AbstractMagazineGun implements IAbility {

	public LaserRifle(IEntityController _game, ICanShoot shooter) {
		super(_game, "Laser Rifle", shooter, .2f, 2, 10);
	}

	
	@Override
	public void launchBullet() {
		//todo - use existing stored bullet 
		// new LaserBullet(game, shooter);
		
	}
	

}
