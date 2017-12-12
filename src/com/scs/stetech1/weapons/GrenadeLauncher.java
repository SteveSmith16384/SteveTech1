package com.scs.stetech1.weapons;

import java.util.LinkedList;

import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.shared.IAbility;
import com.scs.stetech1.shared.IEntityController;
import com.scs.testgame.entities.Grenade;

public class GrenadeLauncher extends AbstractMagazineGun implements IAbility {

	private static final int MAG_SIZE = 6;
	
	private LinkedList<Grenade> ammoCache = new LinkedList<Grenade>(); 
	
	public GrenadeLauncher(IEntityController game, int num, ICanShoot shooter) {
		super(game, num, "GrenadeLauncher", shooter, 1, 3, MAG_SIZE);
	}
	

	@Override
	public void process(float tpf_secs) {
		super.process(tpf_secs);
		
		if (this.ammoCache.size() <= 2) {
			// todo
		}
	}
	

	@Override
	public void launchBullet() {
		if (game.isServer()) { // Client gets told to create as per typical entity
			new Grenade(game, AbstractGameServer.getNextEntityID(), shooter);
		} else { // Client gets told to create as per typical entity
			new Grenade(game, -1, shooter);
		}
	}


}

