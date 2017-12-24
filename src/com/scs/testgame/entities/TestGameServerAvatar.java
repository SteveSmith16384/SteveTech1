package com.scs.testgame.entities;

import com.jme3.scene.Spatial;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.shared.IEntityController;

public class TestGameServerAvatar extends AbstractServerAvatar {

	public TestGameServerAvatar(IEntityController _module, int _playerID, IInputDevice _input, int eid, int side) {
		super(_module, _playerID, _input, eid, side);
	}
	

	@Override
	protected Spatial getPlayersModel(IEntityController game, int pid) {
		return TestGameClientAvatar.getPlayersModel_Static(game, pid);
	}

}
