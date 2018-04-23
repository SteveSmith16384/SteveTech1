package twoweeks.abilities;

import java.util.HashMap;
import java.util.LinkedList;

import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractPlayersBullet;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.weapons.AbstractMagazineGun;

import twoweeks.client.TwoWeeksClientEntityCreator;
import twoweeks.entities.PlayersBullet;

public class PlayersMachineGun extends AbstractMagazineGun<PlayersBullet> implements IAbility, IEntityContainer<PlayersBullet> {

	private LinkedList<PlayersBullet> ammoCache = new LinkedList<PlayersBullet>(); 

	public PlayersMachineGun(IEntityController game, int id, int playerID, AbstractAvatar owner, int avatarID, int abilityNum, ClientData client) {
		super(game, id, TwoWeeksClientEntityCreator.MACHINE_GUN, playerID, owner, avatarID, abilityNum, "Laser Rifle", .2f, 2, 2, client);

	}


	@Override
	public boolean launchBullet() {
		if (!ammoCache.isEmpty()) {
			PlayersBullet bullet = ammoCache.remove();
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
	public void addToCache(PlayersBullet o) {
		this.ammoCache.add(o);
	}


	public void remove() {
		while (!ammoCache.isEmpty()) {
			PlayersBullet g = ammoCache.remove();
			g.remove();
		}
		super.remove();
	}


	@Override
	protected void createBullet(AbstractEntityServer server, int entityid, int playerID, IEntityContainer<AbstractPlayersBullet> irac, int side) {
		PlayersBullet l = new PlayersBullet(game, entityid, playerID, irac, side, client, null);
		server.addEntity(l);

	}
	

	@Override
	public int getBulletsInMag() {
		return this.ammoCache.size();
	}



}
