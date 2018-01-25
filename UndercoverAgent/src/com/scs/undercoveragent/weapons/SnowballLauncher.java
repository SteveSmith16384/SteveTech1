package com.scs.undercoveragent.weapons;

import java.util.HashMap;
import java.util.LinkedList;

import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IRequiresAmmoCache;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.weapons.AbstractMagazineGun;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;
import com.scs.undercoveragent.entities.SnowballBullet;

public class SnowballLauncher extends AbstractMagazineGun implements IAbility, IRequiresAmmoCache<SnowballBullet> {

	private static final int MAG_SIZE = 6;

	private LinkedList<SnowballBullet> ammoCache = new LinkedList<SnowballBullet>(); 

	public SnowballLauncher(IEntityController game, int id, AbstractAvatar owner, int num) {
		super(game, id, UndercoverAgentClientEntityCreator.SNOWBALL_LAUNCHER, owner, num, "SnowballLauncher", 1, 3, MAG_SIZE);

	}


	@Override
	public boolean launchBullet() {
		if (!ammoCache.isEmpty()) {
			SnowballBullet g = ammoCache.remove();
			g.launch((ICanShoot)owner);
			return true;
		}
		return false;
	}


	@Override
	public int getAmmoType() {
		return UndercoverAgentClientEntityCreator.SNOWBALL_BULLET;
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
	public void addToCache(SnowballBullet o) {
		this.ammoCache.add(o);
		
	}


	@Override
	public String getAvatarAnimationCode() {
		return null;
	}

	
	public void remove() {
		while (!ammoCache.isEmpty()) {
			SnowballBullet g = ammoCache.remove();
			g.remove();
		}
		super.remove();
	}
		

}

