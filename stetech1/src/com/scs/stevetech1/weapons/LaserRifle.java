package com.scs.stevetech1.weapons;

import java.util.HashMap;
import java.util.LinkedList;

import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IRequiresAmmoCache;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.LaserBullet;
import com.scs.stevetech1.shared.AbstractClientEntityCreator;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;

/*
 * This gun shoots physical laser bolts
 */
public class LaserRifle extends AbstractMagazineGun implements IAbility, IRequiresAmmoCache<LaserBullet> {

	private LinkedList<LaserBullet> ammoCache = new LinkedList<LaserBullet>(); 
	
	public LaserRifle(IEntityController game, int id, AbstractAvatar owner, int num) {
		super(game, id, AbstractClientEntityCreator.LASER_RIFLE, owner, num, "Laser Rifle", .2f, 2, 10);

	}


	@Override
	public void launchBullet() {
		LaserBullet g = ammoCache.remove();
		g.launch((ICanShoot)owner);
	}




	@Override
	public int getAmmoType() {
		return AbstractClientEntityCreator.LASER_BULLET;
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
