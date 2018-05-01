package com.scs.unittestgame.entities;

import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.unittestgame.UnitTestGameServer;

public class EnemyAvatarEntity extends AbstractEnemyAvatar {
	
	public EnemyAvatarEntity(IEntityController game, int eid, float x, float y, float z, int side) {
		super(game, UnitTestGameServer.AVATAR_ID, eid, x, y, z, null, 0);
	}

	@Override
	public void setAnimCode(int animCode) {
		
	}

	@Override
	public void processManualAnimation(float tpf_secs) {
		
	}

}
