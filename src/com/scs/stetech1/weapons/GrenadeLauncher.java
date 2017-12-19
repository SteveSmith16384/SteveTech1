package com.scs.stetech1.weapons;

import java.util.HashMap;
import java.util.LinkedList;

import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.components.IRequiresAmmoCache;
import com.scs.stetech1.entities.AbstractAvatar;
import com.scs.stetech1.shared.IAbility;
import com.scs.stetech1.shared.IEntityController;
import com.scs.testgame.TestGameEntityCreator;
import com.scs.testgame.entities.Grenade;

public class GrenadeLauncher extends AbstractMagazineGun implements IAbility, IRequiresAmmoCache<Grenade> {

	private static final int MAG_SIZE = 6;

	private LinkedList<Grenade> ammoCache = new LinkedList<Grenade>(); 

	public GrenadeLauncher(IEntityController game, int id, AbstractAvatar owner, int num) {
		super(game, id, TestGameEntityCreator.GRENADE_LAUNCHER, owner, num, "GrenadeLauncher", 1, 3, MAG_SIZE);

	}


	@Override
	public void launchBullet() {
		Grenade g = ammoCache.remove();
		g.launch((ICanShoot)owner);
	}


	@Override
	public int getAmmoType() {
		return TestGameEntityCreator.GRENADE;
	}

	@Override
	public boolean requiresAmmo() {
		return this.ammoCache.size() <= 2;
	}


	@Override
	public HashMap<String, Object> getCreationData() {
		return super.creationData;
	}


	@Override
	public void addToCache(Grenade o) {
		this.ammoCache.add(o);
		
	}


}

