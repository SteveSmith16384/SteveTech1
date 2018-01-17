package com.scs.stevetech1.weapons;

import java.util.HashMap;
import java.util.LinkedList;

import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IRequiresAmmoCache;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.Grenade;
import com.scs.stevetech1.shared.AbstractClientEntityCreator;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;

public class GrenadeLauncher extends AbstractMagazineGun implements IAbility, IRequiresAmmoCache<Grenade> {

	private static final int MAG_SIZE = 6;

	private LinkedList<Grenade> ammoCache = new LinkedList<Grenade>(); 

	public GrenadeLauncher(IEntityController game, int id, AbstractAvatar owner, int num) {
		super(game, id, AbstractClientEntityCreator.GRENADE_LAUNCHER, owner, num, "GrenadeLauncher", 1, 3, MAG_SIZE);

	}


	@Override
	public void launchBullet() {
		Grenade g = ammoCache.remove();
		g.launch((ICanShoot)owner);
	}


	@Override
	public int getAmmoType() {
		return AbstractClientEntityCreator.GRENADE;
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


	@Override
	public String getAvatarAnimationCode() {
		return "LaunchGrenade";
	}


}

