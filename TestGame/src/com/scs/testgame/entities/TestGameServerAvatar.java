package com.scs.testgame.entities;

import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.testgame.models.CharacterModel;

public class TestGameServerAvatar extends AbstractServerAvatar {
	
	public TestGameServerAvatar(IEntityController _module, ClientData client, int _playerID, IInputDevice _input, int eid, int side) {
		super(_module, client, _playerID, _input, eid, side, new CharacterModel(_module.getAssetManager()));
	}
	
/*
	@Override
	protected Spatial getPlayersModel(IEntityController game, int pid) {
		//return TestGameClientAvatar.getPlayersModel_Static(game, pid);

		// Just use a box, no need for anything complicated (yet)
		Box box1 = new Box(ZombieModel.ZOMBIE_MODEL_WIDTH/2, ZombieModel.ZOMBIE_MODEL_HEIGHT/2, ZombieModel.ZOMBIE_MODEL_DEPTH/2);
		Geometry geometry = new Geometry("Crate", box1);
		geometry.setLocalTranslation(0, ZombieModel.ZOMBIE_MODEL_HEIGHT/2, 0); // Move origin to floor
		
		return geometry;

	}
*/
/*
	@Override
	public Vector3f getBulletStartPos() {
		return this.getWorldTranslation().add(0, ZombieModel.ZOMBIE_MODEL_HEIGHT - 0.1f, 0);
	}
*/
}
