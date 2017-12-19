package com.scs.testgame.entities;

import com.jme3.scene.Spatial;
import com.scs.stetech1.entities.AbstractEnemyAvatar;
import com.scs.stetech1.shared.IEntityController;

public class TestGameEnemyPlayersAvatar extends AbstractEnemyAvatar {

	public TestGameEnemyPlayersAvatar(IEntityController game, int pid, int eid, float x, float y, float z) {
		super(game, pid, eid, x, y, z);
	}

	
	@Override
	protected Spatial getPlayersModel(IEntityController game, int pid) {
		return TestGameClientAvatar.getPlayersModel_Static(game, pid);
	}

}
