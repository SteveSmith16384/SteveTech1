package com.scs.stetech1.netmessages;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.scs.stetech1.input.IInputDevice;

@Serializable
public class PlayerInputMessage extends MyAbstractMessage {

	public Vector3f direction, leftDir;
	public boolean fwd, back, strafeLeft, strafeRight, jump, mainAbility, secondaryAbility, selectNextAbility;

	public PlayerInputMessage() {
		super(false);
		
		direction = Vector3f.UNIT_Y; // To avoid NPEs from RemoteController
		leftDir = Vector3f.UNIT_Z; // To avoid NPEs from RemoteController
	}
	
	
	public PlayerInputMessage(IInputDevice inputs) {
		super(false);

		direction = inputs.getDirection();
		leftDir = inputs.getLeft();
		fwd = inputs.getFwdValue();
		back = inputs.getBackValue();
		strafeLeft = inputs.getStrafeLeftValue();
		strafeRight = inputs.getStrafeRightValue();
		jump = inputs.isJumpPressed();
		this.mainAbility = inputs.isShootPressed();
		this.secondaryAbility = inputs.isAbilityOtherPressed();
		this.selectNextAbility = inputs.isSelectNextAbilityPressed();
	}


}
