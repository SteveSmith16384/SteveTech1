package com.scs.testgame.weapons;

import java.util.HashMap;
import java.util.LinkedList;

import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IRequiresAmmoCache;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.weapons.AbstractMagazineGun;
import com.scs.testgame.TestGameClientEntityCreator;
import com.scs.testgame.entities.LaserBullet;

/*
 * This gun shoots physical laser bolts
 */
public class LaserRifle extends AbstractMagazineGun implements IAbility, IRequiresAmmoCache<LaserBullet> {

	private LinkedList<LaserBullet> ammoCache = new LinkedList<LaserBullet>(); 

	public LaserRifle(IEntityController game, int id, AbstractAvatar owner, int num) {
		super(game, id, TestGameClientEntityCreator.LASER_RIFLE, owner, num, "Laser Rifle", .2f, 2, 10);

	}


	@Override
	public boolean launchBullet() {
		if (!ammoCache.isEmpty()) {
			LaserBullet g = ammoCache.remove();
			g.launch((ICanShoot)owner);
			return true;
		}
		return false;
	}




	@Override
	public int getAmmoType() {
		return TestGameClientEntityCreator.LASER_BULLET;
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
	public void addToCache(LaserBullet o) {
		this.ammoCache.add(o);
	}


	@Override
	public String getAvatarAnimationCode() {
		return "Shoot";
	}


}
