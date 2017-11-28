package com.scs.testgame.entities;

import com.jme3.scene.Spatial;
import com.scs.stetech1.entities.ServerPlayersAvatar;
import com.scs.stetech1.input.IInputDevice;
import com.scs.stetech1.shared.IEntityController;

public class TestGameServerPlayersAvatar extends ServerPlayersAvatar {

	public TestGameServerPlayersAvatar(IEntityController _module, int _playerID, IInputDevice _input, int eid, byte side) {
		super(_module, _playerID, _input, eid, side);
	}
	

	@Override
	protected Spatial getPlayersModel(IEntityController game, int pid) {
		return TestGameClientPlayersAvatar.getPlayersModel_Static(game, pid);
	}

}
