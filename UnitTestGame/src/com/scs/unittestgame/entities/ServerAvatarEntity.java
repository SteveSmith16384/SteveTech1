package com.scs.unittestgame.entities;

import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.unittestgame.UnitTestGameServer;
import com.scs.unittestgame.UnitTestInputDevice;
import com.scs.unittestgame.models.AvatarModel;

public class ServerAvatarEntity extends AbstractServerAvatar {

	public ServerAvatarEntity(IEntityController _module, ClientData _client, int eid) {
		super(_module, UnitTestGameServer.AVATAR_ID, _client, new UnitTestInputDevice(), eid, new AvatarModel(), 1f, 3f, 2f, 0);
		
		UnitTestAbility ability = new UnitTestAbility(_module, _module.getNextEntityID(), _client.getPlayerID(), this, eid);
		_module.addEntity(ability);
	}
}
