package com.scs.stetech1.netmessages;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;

@Serializable
public class PlayerInputMessage extends MyAbstractMessage {

	public Vector3f direction;
	public boolean fwd;

	public PlayerInputMessage() {
		super(false);
	}
	
	
	public PlayerInputMessage(Vector3f _direction, boolean _fwd) {
		super(false);

		direction = _direction;
		fwd = _fwd;
	}


}
