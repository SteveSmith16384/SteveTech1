package com.scs.unittestgame.entities;

import com.scs.stevetech1.avatartypes.PersonAvatar;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.unittestgame.DummyCamera;
import com.scs.unittestgame.UnitTestGameServer;
import com.scs.unittestgame.UnitTestInputDevice;
import com.scs.unittestgame.models.AvatarModel;

public class ClientAvatarEntity extends AbstractClientAvatar {
	
	public ClientAvatarEntity(AbstractGameClient _client, int _playerID, int eid, float x, float y, float z, int side) {
		super(_client, UnitTestGameServer.AVATAR_ID, _playerID, new UnitTestInputDevice(), new DummyCamera(), null, eid, x, y, z, side, new AvatarModel(), new PersonAvatar(_client, new UnitTestInputDevice(), 1f, 1f));
		
	}

}
