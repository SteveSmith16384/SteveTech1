package com.scs.undercoveragent.weapons;

import java.util.HashMap;
import java.util.LinkedList;

import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.weapons.AbstractMagazineGun;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;
import com.scs.undercoveragent.entities.SnowballBullet;

public class SnowballLauncher extends AbstractMagazineGun implements IAbility, IEntityContainer<SnowballBullet> {

	private static final int MAG_SIZE = 6;

	private LinkedList<SnowballBullet> ammoCache = new LinkedList<SnowballBullet>(); 

	public SnowballLauncher(IEntityController game, int id, ICanShoot owner, int num) {
		super(game, id, UndercoverAgentClientEntityCreator.SNOWBALL_LAUNCHER, owner, num, "SnowballLauncher", 1, 3, MAG_SIZE);

	}


	/*
	 * This is called when the player fires the weapon
	 */
	@Override
	public boolean launchBullet() {
		if (!ammoCache.isEmpty()) {
			SnowballBullet g = ammoCache.remove();
			ICanShoot ic = (ICanShoot)owner;
			g.launch(owner, ic.getBulletStartPos(), ic.getShootDir());
			return true;
		}
		return false;
	}


	@Override
	public HashMap<String, Object> getCreationData() {
		return super.creationData;
	}


	@Override
	public String getAvatarAnimationCode() {
		return null;
	}

	
	public void remove() {
		// Remove all owned bullets
		while (!ammoCache.isEmpty()) {
			SnowballBullet g = ammoCache.remove();
			g.remove();
		}
		super.remove();
	}


	@Override
	protected void createBullet(AbstractGameServer server, int entityid, IEntityContainer irac, int side) {
		SnowballBullet pe = new SnowballBullet(game, entityid, irac, side);
		server.addEntity(pe);

		
	}


	@Override
	public int getBulletsInMag() {
		return this.ammoCache.size();
	}


	@Override
	public void addToCache(SnowballBullet o) {
		this.ammoCache.add(o);
	}


}

