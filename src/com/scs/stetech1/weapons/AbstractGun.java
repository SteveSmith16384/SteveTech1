package com.scs.stetech1.weapons;

import ssmith.util.RealtimeInterval;

import com.scs.stetech1.abilities.IAbility;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.shared.IEntityController;

public abstract class AbstractGun implements IAbility {

	protected IEntityController game;
	protected ICanShoot shooter;
	protected String name;
	protected RealtimeInterval shotInterval;

	public AbstractGun(IEntityController _game, String _name, long shotIntervalMS, ICanShoot _shooter) {
		game = _game;
		name = _name;
		shooter = _shooter;
		shotInterval = new RealtimeInterval(shotIntervalMS);
		
	}


	@Override
	public boolean process(float interpol) {
		// Do nothing
		return false;
	}


	@Override
	public String getHudText() {
		return name;
	}

}
