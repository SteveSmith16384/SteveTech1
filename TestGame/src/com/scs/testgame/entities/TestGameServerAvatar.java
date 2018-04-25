package com.scs.testgame.entities;

import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IAbility;
import com.scs.testgame.TestGameServer;
import com.scs.testgame.models.CharacterModel;
import com.scs.testgame.weapons.HitscanRifle;
import com.scs.testgame.weapons.LaserRifle;

public class TestGameServerAvatar extends AbstractServerAvatar {
	
	public TestGameServerAvatar(TestGameServer _module, ClientData _client, int _playerID, IInputDevice _input, int eid) {
		super(_module, _client, _playerID, _input, eid, new CharacterModel(_module.getAssetManager()));

		//IAbility abilityGun = new LaserRifle(_module, _module.getNextEntityID(), client.getPlayerID(), this, eid, 0, client);
		IAbility abilityGun = new HitscanRifle(_module, _module.getNextEntityID(), client.getPlayerID(), this, eid, 0, client);
		_module.actuallyAddEntity(abilityGun);
		
	}
	

}
