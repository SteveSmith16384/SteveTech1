package com.scs.stetech1.weapons;

import com.scs.stetech1.abilities.IAbility;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.entities.LaserBullet;
import com.scs.stetech1.shared.IEntityController;

public class LaserRifle extends AbstractMagazineGun implements IAbility {

	public LaserRifle(IEntityController _game, ICanShoot shooter) {
		super(_game, "Laser Rifle", shooter, .2f, 2, 10);
	}

	
	@Override
	public void launchBullet(IEntityController game, ICanShoot _shooter) {
		new LaserBullet(game, shooter);
		
	}
	

	/*@Override
	public boolean activate(float interpol) {
		if (shotInterval.hitInterval()) {
			new LaserBullet(game, module, shooter);
			return true;
		}
		return false;
	}*/

}
