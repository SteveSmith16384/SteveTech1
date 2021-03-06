package com.scs.stevetech1.netmessages;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.input.IInputDevice;

@Serializable
public class PlayerInputMessage extends MyAbstractMessage {

	public Vector3f direction, leftDir;
	public boolean fwd, back, strafeLeft, strafeRight, jump;//, ability1, ability2;//, selectNextAbility;

	public PlayerInputMessage() {
		super();
		
		direction = Vector3f.UNIT_Y; // To avoid NPEs from RemoteController
		leftDir = Vector3f.UNIT_Z; // To avoid NPEs from RemoteController
	}
	
	
	public PlayerInputMessage(IInputDevice inputs) {
		super(false, false);

		direction = inputs.getDirection();
		leftDir = inputs.getLeft();
		fwd = inputs.getFwdValue();
		back = inputs.getBackValue();
		strafeLeft = inputs.getStrafeLeftValue();
		strafeRight = inputs.getStrafeRightValue();
		jump = inputs.isJumpPressed();
		//this.ability1 = inputs.isAbilityPressed(0);
		//this.ability2 = inputs.isAbilityPressed(1);
		//this.selectNextAbility = inputs.isSelectNextAbilityPressed();
	}


}
