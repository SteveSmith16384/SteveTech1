package com.scs.testgame.weapons;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.weapons.AbstractMagazineGun;
import com.scs.testgame.TestGameClientEntityCreator;
import com.scs.testgame.entities.PlayerLaserBullet;

public class LaserRifle extends AbstractMagazineGun implements IAbility {

	//private LinkedList<PlayerLaserBullet> ammoCache = new LinkedList<PlayerLaserBullet>(); 

	public LaserRifle(IEntityController game, int id, int playerID, AbstractAvatar owner, int avatarID, byte abilityNum, ClientData client) {
		super(game, id, TestGameClientEntityCreator.LASER_RIFLE, playerID, owner, avatarID, abilityNum, "Laser Rifle", .2f, 2, 2, client);

	}

/*
	@Override
	public boolean launchBullet() {
		if (!ammoCache.isEmpty()) {
			PlayerLaserBullet bullet = ammoCache.remove();
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
	public void addToCache(PlayerLaserBullet o) {
		this.ammoCache.add(o);
	}


	@Override
	public void removeFromCache(PlayerLaserBullet o) {
		this.ammoCache.remove(o);
	}


	public void remove() {
		while (!ammoCache.isEmpty()) {
			PlayerLaserBullet g = ammoCache.remove();
			g.remove();
		}
		super.remove();
	}
*/

	@Override
	protected PlayerLaserBullet createBullet(int entityid, int playerID, IEntity _shooter, Vector3f startPos, Vector3f _dir, byte side) {
		return new PlayerLaserBullet(game, entityid, playerID, _shooter, startPos, _dir, side, client);
	}
	
/*
	@Override
	public int getBulletsInMag() {
		return this.ammoCache.size();
	}

/*
	@Override
	protected void emptyMagazine() {
		while (!ammoCache.isEmpty()) {
			PlayerLaserBullet g = ammoCache.remove();
			g.remove();
		}
		
	}

*/

}
