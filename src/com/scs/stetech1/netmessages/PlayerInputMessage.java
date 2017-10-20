package com.scs.stetech1.netmessages;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.scs.stetech1.input.IInputDevice;

@Serializable
public class PlayerInputMessage extends MyAbstractMessage {

	public Vector3f direction;
	public boolean fwd;

	public PlayerInputMessage() {
		super(false, false);
	}
	
	
	public PlayerInputMessage(IInputDevice inputs) {
		super(false, false);

		direction = inputs.getDirection();
		fwd = inputs.getFwdValue();
		// todo - other inputs
	}


}
