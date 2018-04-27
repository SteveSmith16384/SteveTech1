package twoweeks.entities;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.entities.AbstractAIBullet;
import com.scs.stevetech1.entities.AbstractAISoldier;
import com.scs.stevetech1.server.IArtificialIntelligence;
import com.scs.stevetech1.shared.IEntityController;

import twoweeks.client.TwoWeeksClientEntityCreator;
import twoweeks.models.SoldierModel;
import twoweeks.server.ai.ShootingSoldierAI3;

public class TWIB_AISoldier extends AbstractAISoldier {

	public TWIB_AISoldier(IEntityController _game, int id, float x, float y, float z, int _side, int csInitialAnimCode) {
		super(_game, id, TwoWeeksClientEntityCreator.AI_SOLDIER, x, y, z, _side, 
				new SoldierModel(_game.getAssetManager()), csInitialAnimCode);

		if (_game.isServer()) {
			ai = new ShootingSoldierAI3(this);
		}
	}

	
	@Override
	protected AbstractAIBullet createBullet(Vector3f pos, Vector3f dir) {
		AIBullet bullet = new AIBullet(game, game.getNextEntityID(), side, pos.x, pos.y, pos.z, this, dir);
		return bullet;
	}


}

