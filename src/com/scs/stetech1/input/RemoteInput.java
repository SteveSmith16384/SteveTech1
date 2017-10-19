package com.scs.stetech1.input;

import com.jme3.math.Vector3f;

public class RemoteInput implements IInputDevice {

	public RemoteInput() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean getFwdValue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getBackValue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getStrafeLeftValue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getStrafeRightValue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isJumpPressed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isShootPressed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAbilityOtherPressed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSelectNextAbilityPressed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Vector3f getDirection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector3f getLeft() {
		// TODO Auto-generated method stub
		return null;
	}

}
