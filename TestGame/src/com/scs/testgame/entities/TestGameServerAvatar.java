package com.scs.testgame.entities;

import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.testgame.models.CharacterModel;

public class TestGameServerAvatar extends AbstractServerAvatar {
	
	public TestGameServerAvatar(IEntityController _module, ClientData _client, int _playerID, IInputDevice _input, int eid) {
		super(_module, _client, _playerID, _input, eid, new CharacterModel(_module.getAssetManager()));
	}
	

}
