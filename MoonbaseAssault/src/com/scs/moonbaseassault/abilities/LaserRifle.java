package com.scs.moonbaseassault.abilities;

import java.util.HashMap;
import java.util.LinkedList;

import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.entities.LaserBullet;
import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.weapons.AbstractMagazineGun;

/*
 * This gun shoots physical laser bolts
 */
public class LaserRifle extends AbstractMagazineGun<LaserBullet> implements IAbility, IEntityContainer<LaserBullet> {

	private LinkedList<LaserBullet> ammoCache = new LinkedList<LaserBullet>(); 

	public LaserRifle(IEntityController game, int id, AbstractAvatar owner, int abilityNum, ClientData client) {
		super(game, id, MoonbaseAssaultClientEntityCreator.LASER_RIFLE, owner, abilityNum, "Laser Rifle", .2f, 2, 10, client);

	}


	@Override
	public boolean launchBullet() {
		if (!ammoCache.isEmpty()) {
			LaserBullet bullet = ammoCache.remove();
			ICanShoot ic = (ICanShoot)owner;
			bullet.launch(owner, ic.getBulletStartPos(), ic.getShootDir());
			return true;
		}
		return false;
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


	public void remove() {
		while (!ammoCache.isEmpty()) {
			LaserBullet g = ammoCache.remove();
			g.remove();
		}
		super.remove();
	}


	@Override
	protected void createBullet(AbstractEntityServer server, int entityid, IEntityContainer irac, int side) {
		LaserBullet l = new LaserBullet(game, entityid, irac, side, client);
		server.addEntity(l);

	}
	

	@Override
	public int getBulletsInMag() {
		return this.ammoCache.size();
	}



}
