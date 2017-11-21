package com.scs.stetech1.jme;

import com.jme3.math.Vector3f;
import com.scs.stetech1.entities.AbstractPlayersAvatar;

public class MySimpleCharacterControl {
	
	private AbstractPlayersAvatar avatar;
	private Vector3f walkDir = new Vector3f();

	public MySimpleCharacterControl(AbstractPlayersAvatar _avatar) {
		avatar = _avatar;
	}


	public void setWalkDirection(Vector3f dir) {
		walkDir.set(dir);
	}
	
		
	public boolean isOnGround( ) {
		return true; // todo
	}
	
}
