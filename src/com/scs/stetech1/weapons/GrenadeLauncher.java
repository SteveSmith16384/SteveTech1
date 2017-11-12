package com.scs.stetech1.weapons;

import com.scs.stetech1.abilities.IAbility;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.shared.entities.Grenade;

public class GrenadeLauncher extends AbstractGun implements IAbility {

	public GrenadeLauncher(Overwatch _game, GameModule _module, ICanShoot shooter) {
		super(_game, _module, "GrenadeLauncher", 1500, shooter);
	}
	

	@Override
	public boolean activate(float interpol) {
		if (shotInterval.hitInterval()) {
			new Grenade(game, module, shooter);
			return true;
		}
		return false;
	}


}
