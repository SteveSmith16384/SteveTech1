package com.scs.unittestgame;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.input.IInputDevice;

public class UnitTestInputDevice implements IInputDevice {

	private Vector3f dummy = new Vector3f();
	
	@Override
	public Vector3f getDirection() {
		return dummy;
	}

	@Override
	public Vector3f getLeft() {
		return dummy;
	}

	@Override
	public boolean getFwdValue() {
		return false;
	}

	@Override
	public boolean getBackValue() {
		return false;
	}

	@Override
	public boolean getStrafeLeftValue() {
		return false;
	}

	@Override
	public boolean getStrafeRightValue() {
		return false;
	}

	@Override
	public boolean isJumpPressed() {
		return false;
	}

	@Override
	public boolean isAbilityPressed(int i) {
		return false;
	}

}
