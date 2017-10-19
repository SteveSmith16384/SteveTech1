package com.scs.stetech1.server.entities;

import com.jme3.math.Vector3f;
import com.scs.stetech1.input.IInputDevice;
import com.scs.stetech1.shared.IEntityController;
import com.scs.stetech1.shared.AbstractPlayersAvatar;

public class ServerPlayersAvatar extends AbstractPlayersAvatar {

	public ServerPlayersAvatar(IEntityController _module, int _playerID, IInputDevice _input) {
		super(_module, _playerID, _input);
	}

	@Override
	public Vector3f getShootDir() {
		// TODO Auto-generated method stub
		return null;
	}

/*	@Override
	public Vector3f getDirection() {
		return input.g
	}

	@Override
	public Vector3f getLeft() {
		// TODO Auto-generated method stub
		return null;
	}
*/
}
