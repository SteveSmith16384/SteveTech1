package boxwars.weapons;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.weapons.AbstractMagazineGun;

import boxwars.BoxWarsServer;
import boxwars.entities.PlayersBullet;

/*
 * This gun shoots physical bullets (i.e. is not hitscan).
 */
public class PlayersGun extends AbstractMagazineGun implements IAbility {

	//private LinkedList<PlayersBullet> ammoCache = new LinkedList<PlayersBullet>(); 

	public PlayersGun(IEntityController game, int id, int playerID, AbstractAvatar owner, int avatarID, int abilityNum, ClientData client) {
		super(game, id, BoxWarsServer.GUN, playerID, owner, avatarID, abilityNum, "Laser Rifle", .2f, 2, 10, client);

	}

/*
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


	@Override
	public void removeFromCache(PlayersBullet o) {
		this.ammoCache.remove(o);
	}


	public void remove() {
		while (!ammoCache.isEmpty()) {
			PlayersBullet g = ammoCache.remove();
			g.remove();
		}
		super.remove();
	}

*/
	@Override
	protected PlayersBullet createBullet(int entityid, int playerID, IEntity _shooter, Vector3f startPos, Vector3f _dir, int side) {
		return new PlayersBullet(game, entityid, playerID, _shooter, startPos, _dir, side, client);
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
			PlayersBullet g = ammoCache.remove();
			g.remove();
		}		
	}
*/

}
