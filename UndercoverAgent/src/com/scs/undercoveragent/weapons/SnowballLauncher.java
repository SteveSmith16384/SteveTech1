package com.scs.undercoveragent.weapons;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.weapons.AbstractMagazineGun;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;
import com.scs.undercoveragent.entities.SnowballBullet;

public class SnowballLauncher extends AbstractMagazineGun implements IAbility {

	private static final int MAG_SIZE = 999;

	public SnowballLauncher(IEntityController game, int id, int playerID, ICanShoot owner, int avatarID, byte num, ClientData _client) { // ClientData is null on client!
		super(game, id, UndercoverAgentClientEntityCreator.SNOWBALL_LAUNCHER, playerID, owner, avatarID, num, "SnowballLauncher", 1, 3, MAG_SIZE, _client);
		
	}


	@Override
	protected SnowballBullet createBullet(int entityid, int playerID, IEntity _shooter, Vector3f startPos, Vector3f _dir, byte side) {
		return new SnowballBullet(game, entityid, playerID, _shooter, startPos, _dir, side, client);
	}
	

}

