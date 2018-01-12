package com.scs.testgame.entities;

import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.hud.HUD;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoverzombie.ZombieAnimationStrings;
import com.scs.undercoverzombie.models.ZombieModel;

public class TestGameClientAvatar extends AbstractClientAvatar {

	private ZombieModel zm;

	public TestGameClientAvatar(AbstractGameClient _module, int _playerID, IInputDevice _input, Camera _cam, HUD _hud, int eid, float x, float y, float z, int side) {
		super(_module, _playerID, _input, _cam, _hud, eid, x, y, z, side, new ZombieAnimationStrings());
		
	}


	@Override
	protected Spatial getPlayersModel(IEntityController game, int pid) {
		if (zm == null)
		{
			zm = new ZombieModel(game.getAssetManager());
		}
		return zm.getModel();
	}

	
}
