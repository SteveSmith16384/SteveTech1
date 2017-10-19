package com.scs.stetech1.input;

import com.jme3.math.Vector3f;
import com.scs.stetech1.netmessages.PlayerInputMessage;

public class RemoteInput implements IInputDevice {

	private boolean fwd;
	private Vector3f dir = new Vector3f(0, 0, -1);
	private Vector3f leftDir = new Vector3f(0, -1, 0);
	
	public RemoteInput() {

	}
	
	
	public void decodeMessage(PlayerInputMessage pim) {
		this.dir = pim.direction;
		this.fwd = pim.fwd;
		
	}

	@Override
	public boolean getFwdValue() {
		return fwd;
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
		return dir;
	}

	@Override
	public Vector3f getLeft() {
		return leftDir;
	}

}
