package com.scs.moonbaseassault.weapons;

import java.util.HashMap;
import java.util.LinkedList;

import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.entities.Grenade;
import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.weapons.AbstractMagazineGun;

public class GrenadeLauncher extends AbstractMagazineGun<Grenade> implements IAbility, IEntityContainer<Grenade> {

	private static final int MAG_SIZE = 6;

	private LinkedList<Grenade> ammoCache = new LinkedList<Grenade>();

	public GrenadeLauncher(IEntityController game, int id, ICanShoot owner, int num, ClientData _client) {
		super(game, id, MoonbaseAssaultClientEntityCreator.GRENADE_LAUNCHER, owner, num, "GrenadeLauncher", 1, 3, MAG_SIZE, _client);
		
	}


	/*
	 * This is called when the player fires the weapon
	 */
	@Override
	public boolean launchBullet() {
		if (!ammoCache.isEmpty()) {
			Grenade g = ammoCache.remove();
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


	@Override
	public void remove() {
		// Remove all owned bullets
		while (!ammoCache.isEmpty()) {
			Grenade g = ammoCache.remove();
			g.remove();
		}
		super.remove();
	}


	@Override
	protected void createBullet(AbstractEntityServer server, int entityid, IEntityContainer irac, int side) {
		Grenade pe = new Grenade(game, entityid, irac, side, client);
		server.addEntity(pe);


	}


	@Override
	public int getBulletsInMag() {
		return this.ammoCache.size();
	}


	@Override
	public void addToCache(Grenade o) {
		this.ammoCache.add(o);
	}


}

