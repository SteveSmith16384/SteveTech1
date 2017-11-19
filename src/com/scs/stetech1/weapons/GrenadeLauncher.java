package com.scs.stetech1.weapons;

import com.scs.stetech1.IAbility;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.server.ServerMain;
import com.scs.stetech1.shared.IEntityController;
import com.scs.testgame.entities.Grenade;

public class GrenadeLauncher extends AbstractMagazineGun implements IAbility {

	public GrenadeLauncher(IEntityController game, ICanShoot shooter) {
		super(game, "GrenadeLauncher", shooter, 1000, 2000, 6);
	}
	

	@Override
	public void launchBullet() {
		if (game.isServer()) { // Client gets told to create as per typical entity
			new Grenade(game, ServerMain.getNextEntityID(), shooter);
		}
	}


}

