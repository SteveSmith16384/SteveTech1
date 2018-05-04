package com.scs.testgame.entities;

import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.weapons.HitscanRifle;
import com.scs.testgame.TestGameClientEntityCreator;
import com.scs.testgame.TestGameServer;
import com.scs.testgame.models.CharacterModel;

public class TestGameServerAvatar extends AbstractServerAvatar {
	
	public TestGameServerAvatar(TestGameServer _module, ClientData _client, IInputDevice _input, int eid) {
		super(_module, TestGameClientEntityCreator.AVATAR, _client, _input, eid, new CharacterModel(_module.getAssetManager()), 1f, 3f, 2f);

		//IAbility abilityGun = new LaserRifle(_module, _module.getNextEntityID(), client.getPlayerID(), this, eid, 0, client);
		IAbility abilityGun = new HitscanRifle(_module, _module.getNextEntityID(), TestGameClientEntityCreator.HITSCAN_RIFLE, client.getPlayerID(), this, eid, 0, client, TestGameClientEntityCreator.BULLET_TRAIL, TestGameClientEntityCreator.DEBUGGING_SPHERE);
		_module.actuallyAddEntity(abilityGun);
		
	}
	

}
