package com.scs.testgame.entities;

import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoverzombie.ZombieAnimationStrings;

public class TestGameServerAvatar extends AbstractServerAvatar {
	
	private static final float w = 1; // todo
	private static final float d = 1;
	private static final float h = 1;

	public TestGameServerAvatar(IEntityController _module, int _playerID, IInputDevice _input, int eid, int side) {
		super(_module, _playerID, _input, eid, side, new ZombieAnimationStrings());
	}
	

	@Override
	protected Spatial getPlayersModel(IEntityController game, int pid) {
		//return TestGameClientAvatar.getPlayersModel_Static(game, pid);

		// Just use a box, no need for anything complicated (yet)
		Box box1 = new Box(w/2, h/2, d/2);
		Geometry geometry = new Geometry("Crate", box1);
		geometry.setLocalTranslation(0, h/2, 0);
		
		return geometry;

	}

}
