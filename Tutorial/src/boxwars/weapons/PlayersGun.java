package boxwars.weapons;

import java.util.HashMap;
import java.util.LinkedList;

import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractPlayersBullet;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.weapons.AbstractMagazineGun;

import boxwars.BoxWarsServer;
import boxwars.entities.PlayersBullet;

/*
 * This gun shoots physical bullets (i.e. is not hitscan).
 */
public class PlayersGun extends AbstractMagazineGun<PlayersBullet> implements IAbility, IEntityContainer<PlayersBullet> {

	private LinkedList<PlayersBullet> ammoCache = new LinkedList<PlayersBullet>(); 

	public PlayersGun(IEntityController game, int id, int playerID, AbstractAvatar owner, int avatarID, int abilityNum, ClientData client) {
		super(game, id, BoxWarsServer.GUN, playerID, owner, avatarID, abilityNum, "Laser Rifle", .2f, 2, 10, client);

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
	protected void createBullet(AbstractGameServer server, int entityid, int playerID, IEntityContainer<AbstractPlayersBullet> owner, int side) {
		PlayersBullet l = new PlayersBullet(game, entityid, playerID, owner, side, client);
		server.addEntity(l);

	}
	

	@Override
	public int getBulletsInMag() {
		return this.ammoCache.size();
	}


	@Override
	protected void emptyMagazine() {
		while (!ammoCache.isEmpty()) {
			PlayersBullet g = ammoCache.remove();
			g.remove();
		}		
	}


}
