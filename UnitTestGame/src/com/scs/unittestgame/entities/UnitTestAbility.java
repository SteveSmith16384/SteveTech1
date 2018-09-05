package com.scs.unittestgame.entities;

import java.util.HashMap;

import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.netmessages.AbilityUpdateMessage;
import com.scs.stevetech1.shared.AbstractAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.unittestgame.UnitTestGameServer;

public class UnitTestAbility extends AbstractAbility {
	
	public UnitTestAbility(IEntityController _game, int _id, int _playerID, AbstractAvatar _owner, int _avatarID) {
		super(_game, _id, UnitTestGameServer.ABILITY_ID, _playerID, _owner, _avatarID, 0, "Flange", 0);

		if (game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("avatarID", _avatarID);
			creationData.put("playerID", playerID);
		}
		
	}

	@Override
	public boolean activate() {
		return true;
	}

	@Override
	public String getHudText() {
		return null;
	}

	@Override
	public void encode(AbilityUpdateMessage aum) {
		
	}

	@Override
	public void decode(AbilityUpdateMessage aum) {
		
	}

}
