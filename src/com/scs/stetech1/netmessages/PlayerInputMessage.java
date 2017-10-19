package com.scs.stetech1.netmessages;

import com.jme3.math.Vector3f;

public class PlayerInputMessage extends MyAbstractMessage {

	public Vector3f direction;
	
	public PlayerInputMessage(boolean fwd, Vector3f _direction) {
		super(false);

		direction = _direction;
	}


}
