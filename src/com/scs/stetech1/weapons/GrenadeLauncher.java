package com.scs.stetech1.weapons;

import com.scs.stetech1.abilities.IAbility;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.shared.IEntityController;

public class GrenadeLauncher extends AbstractMagazineGun implements IAbility {

	public GrenadeLauncher(IEntityController game, ICanShoot shooter) {
		super(game, "GrenadeLauncher", shooter, 1000, 2000, 6);
	}
	

	@Override
	public void launchBullet() {
		//todo new Grenade(game, shooter);
	}


}
