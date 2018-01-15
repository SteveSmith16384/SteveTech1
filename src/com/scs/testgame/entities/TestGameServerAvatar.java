package com.scs.testgame.entities;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoverzombie.ZombieAnimationStrings;

public class TestGameServerAvatar extends AbstractServerAvatar {
	
	// Dimensions for zombie model
	private static final float ZOMBIE_MODEL_WIDTH = .3f;
	private static final float ZOMBIE_MODEL_DEPTH = .3f;
	public static final float ZOMBIE_MODEL_HEIGHT = .7f;

	public TestGameServerAvatar(IEntityController _module, int _playerID, IInputDevice _input, int eid, int side) {
		super(_module, _playerID, _input, eid, side, new ZombieAnimationStrings());
	}
	

	@Override
	protected Spatial getPlayersModel(IEntityController game, int pid) {
		//return TestGameClientAvatar.getPlayersModel_Static(game, pid);

		// Just use a box, no need for anything complicated (yet)
		Box box1 = new Box(ZOMBIE_MODEL_WIDTH/2, ZOMBIE_MODEL_HEIGHT/2, ZOMBIE_MODEL_DEPTH/2);
		Geometry geometry = new Geometry("Crate", box1);
		geometry.setLocalTranslation(0, ZOMBIE_MODEL_HEIGHT/2, 0);
		
		return geometry;

	}


	@Override
	public Vector3f getBulletStartPos() {
		return this.getWorldTranslation().add(0, TestGameServerAvatar.ZOMBIE_MODEL_HEIGHT - 0.1f, 0);
	}

}
