package com.scs.testgame.entities;

import com.jme3.renderer.Camera;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.hud.HUD;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.testgame.models.CharacterModel;

public class TestGameClientAvatar extends AbstractClientAvatar {

	public TestGameClientAvatar(AbstractGameClient _module, int _playerID, IInputDevice _input, Camera _cam, HUD _hud, int eid, float x, float y, float z, int side) {
		super(_module, _playerID, _input, _cam, _hud, eid, x, y, z, side, new CharacterModel(_module.getAssetManager()));
		
	}

}
