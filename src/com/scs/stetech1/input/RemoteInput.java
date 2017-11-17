package com.scs.stetech1.input;

import com.jme3.math.Vector3f;
import com.scs.stetech1.netmessages.PlayerInputMessage;

public class RemoteInput implements IInputDevice {

	private PlayerInputMessage pim = new PlayerInputMessage(); // create default so we don't get an NPE

	public RemoteInput() {

	}


	public void decodeMessage(PlayerInputMessage _pim) {
		pim = _pim;

	}

	@Override
	public boolean getFwdValue() {
		return pim.fwd;
	}

	@Override
	public boolean getBackValue() {
		return pim.back;
	}


	@Override
	public boolean getStrafeLeftValue() {
		return pim.strafeLeft;
	}


	@Override
	public boolean getStrafeRightValue() {
		return pim.strafeRight;
	}


	@Override
	public boolean isJumpPressed() {
		return pim.jump;
	}


	@Override
	public boolean isShootPressed() {
		return pim.mainAbility;
	}


	@Override
	public boolean isAbilityOtherPressed() {
		return pim.secondaryAbility;
	}


	@Override
	public boolean isSelectNextAbilityPressed() {
		return pim.selectNextAbility;
	}


	@Override
	public Vector3f getDirection() {
		return pim.direction;
	}


	@Override
	public Vector3f getLeft() {
		return pim.leftDir;
	}

}
