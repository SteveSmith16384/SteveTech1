package com.scs.unittestgame.entities;

import com.scs.stevetech1.avatartypes.PersonAvatarControl;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.unittestgame.DummyCamera;
import com.scs.unittestgame.UnitTestGameServer;
import com.scs.unittestgame.UnitTestInputDevice;
import com.scs.unittestgame.models.AvatarModel;

public class ClientAvatarEntity extends AbstractClientAvatar {
	
	public ClientAvatarEntity(AbstractGameClient _client, int _playerID, int eid, float x, float y, float z, byte side) {
		super(_client, UnitTestGameServer.AVATAR_ID, _playerID, new UnitTestInputDevice(), new DummyCamera(), eid, x, y, z, side, new AvatarModel(), new PersonAvatarControl(_client, new UnitTestInputDevice(), 1f, 1f));
		
	}

}
