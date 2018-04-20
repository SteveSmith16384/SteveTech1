package com.scs.undercoveragent.weapons;

import java.util.HashMap;
import java.util.LinkedList;

import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.weapons.AbstractMagazineGun;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;
import com.scs.undercoveragent.entities.SnowballBullet;

public class SnowballLauncher extends AbstractMagazineGun<SnowballBullet> implements IAbility, IEntityContainer<SnowballBullet> {

	private static final int MAG_SIZE = 6;

	private LinkedList<SnowballBullet> ammoCache = new LinkedList<SnowballBullet>();

	public SnowballLauncher(IEntityController game, int id, int playerID, ICanShoot owner, int num, ClientData _client) { // ClientData is null on client!
		super(game, id, UndercoverAgentClientEntityCreator.SNOWBALL_LAUNCHER, playerID, owner, num, "SnowballLauncher", 1, 3, MAG_SIZE, _client);
		
	}


	/*
	 * This is called when the player fires the weapon
	 */
	@Override
	public boolean launchBullet() {
		if (!ammoCache.isEmpty()) {
			SnowballBullet g = ammoCache.remove();
			ICanShoot ic = (ICanShoot)owner;
			/*if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
				Globals.p("Manually launching entity " + g.id);
			}*/
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
			SnowballBullet g = ammoCache.remove();
			g.remove();
		}
		super.remove();
	}


	@Override
	protected void createBullet(AbstractEntityServer server, int entityid, int playerID, IEntityContainer irac, int side) {
		SnowballBullet pe = new SnowballBullet(game, entityid, playerID, irac, side, client);
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

