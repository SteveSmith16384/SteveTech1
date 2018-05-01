package com.scs.unittestgame.entities;

import com.scs.stevetech1.components.IAvatarModel;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IEntityController;

public class AvatarEntity extends AbstractServerAvatar {

	public AvatarEntity(IEntityController _module, ClientData _client, int _playerID, int eid) {
		super(_module, _client, -1, null, eid, null);
	}
}
