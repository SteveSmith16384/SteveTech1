package com.scs.testgame.entities;

import com.jme3.scene.Spatial;
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.shared.IEntityController;

public class TestGameEnemyAvatar extends AbstractEnemyAvatar {

	public TestGameEnemyAvatar(IEntityController game, int pid, int eid, float x, float y, float z) {
		super(game, pid, eid, x, y, z);
	}

	
	@Override
	protected Spatial getPlayersModel(IEntityController game, int pid) {
		return TestGameClientAvatar.getPlayersModel_Static(game, pid);
	}

}
