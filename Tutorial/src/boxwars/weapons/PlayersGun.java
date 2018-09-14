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

	public PlayersGun(IEntityController game, int id, int playerID, AbstractAvatar owner, int avatarID, byte abilityNum, ClientData client) {
		super(game, id, BoxWarsServer.GUN, playerID, owner, avatarID, abilityNum, "Laser Rifle", .2f, 2, 10, client);

	}


	@Override
	protected PlayersBullet createBullet(int entityid, int playerID, IEntity _shooter, Vector3f startPos, Vector3f _dir, byte side) {
		return new PlayersBullet(game, entityid, playerID, _shooter, startPos, _dir, side, client);
	}

}
