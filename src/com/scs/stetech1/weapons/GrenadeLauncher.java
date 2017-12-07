package com.scs.stetech1.weapons;

import com.jme3.math.Vector3f;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.shared.IAbility;
import com.scs.stetech1.shared.IEntityController;
import com.scs.testgame.entities.Grenade;

public class GrenadeLauncher extends AbstractMagazineGun implements IAbility {

	private static final int MAG_SIZE = 1000; // todo - check
	
	public GrenadeLauncher(IEntityController game, ICanShoot shooter) {
		super(game, "GrenadeLauncher", shooter, 1, 3, MAG_SIZE);
	}
	

	@Override
	public void launchBullet() {
		if (game.isServer()) { // Client gets told to create as per typical entity
			new Grenade(game, AbstractGameServer.getNextEntityID(), shooter);
		}
	}


}

