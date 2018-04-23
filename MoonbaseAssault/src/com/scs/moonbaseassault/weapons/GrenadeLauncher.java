package com.scs.moonbaseassault.weapons;

import java.util.HashMap;
import java.util.LinkedList;

import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.entities.PlayersGrenade;
import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.weapons.AbstractMagazineGun;

public class GrenadeLauncher extends AbstractMagazineGun<PlayersGrenade> implements IAbility, IEntityContainer<PlayersGrenade> {

	private static final int MAG_SIZE = 6;

	private LinkedList<PlayersGrenade> ammoCache = new LinkedList<PlayersGrenade>();

	public GrenadeLauncher(IEntityController game, int id, int playerID, ICanShoot owner, int num, ClientData _client) {
		super(game, id, MoonbaseAssaultClientEntityCreator.GRENADE_LAUNCHER, playerID, owner, num, "GrenadeLauncher", 1, 3, MAG_SIZE, _client);
		
	}


	/*
	 * This is called when the player fires the weapon
	 */
	@Override
	public boolean launchBullet() {
		if (!ammoCache.isEmpty()) {
			PlayersGrenade g = ammoCache.remove();
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
	public void remove() {
		// Remove all owned bullets
		while (!ammoCache.isEmpty()) {
			PlayersGrenade g = ammoCache.remove();
			g.remove();
		}
		super.remove();
	}


	@Override
	protected void createBullet(AbstractEntityServer server, int entityid, int playerID, IEntityContainer irac, int side) {
		PlayersGrenade pe = new PlayersGrenade(game, entityid, playerID, irac, side, client);
		server.addEntity(pe);


	}


	@Override
	public int getBulletsInMag() {
		return this.ammoCache.size();
	}


	@Override
	public void addToCache(PlayersGrenade o) {
		this.ammoCache.add(o);
	}


}

