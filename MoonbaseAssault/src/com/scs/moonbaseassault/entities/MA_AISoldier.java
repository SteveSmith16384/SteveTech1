package com.scs.moonbaseassault.entities;

import com.jme3.math.Vector3f;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.models.SoldierModel;
import com.scs.moonbaseassault.server.ai.WanderingSoldierAI2;
import com.scs.stevetech1.entities.AbstractAIBullet;
import com.scs.stevetech1.entities.AbstractAISoldier;
import com.scs.stevetech1.shared.IEntityController;

public class MA_AISoldier extends AbstractAISoldier {
	
	public MA_AISoldier(IEntityController _game, int id, float x, float y, float z, int _side, int csInitialAnimCode) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.AI_SOLDIER, x, y, z, _side, 
				new SoldierModel(_game.getAssetManager()), csInitialAnimCode);

		if (_game.isServer()) {
			ai = new WanderingSoldierAI2(this);
		}
	}

	@Override
	protected AbstractAIBullet createBullet(Vector3f pos, Vector3f dir) {
		return null; // AI doesn't shoot (yet)
	}

}
