package com.scs.unittestgame.entities;

import com.scs.stevetech1.entities.AbstractOtherPlayersAvatar;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.unittestgame.UnitTestGameServer;
import com.scs.unittestgame.models.AvatarModel;

public class EnemyAvatarEntity extends AbstractOtherPlayersAvatar {
	
	public EnemyAvatarEntity(IEntityController game, int eid, float x, float y, float z, byte side) {
		super(game, UnitTestGameServer.AVATAR_ID, eid, x, y, z, new AvatarModel(), side, "UnitTest");
	}

}
