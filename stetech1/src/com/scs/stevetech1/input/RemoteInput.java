package com.scs.stevetech1.input;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.netmessages.PlayerInputMessage;
import com.scs.stevetech1.server.Globals;

public class RemoteInput implements IInputDevice {

	private PlayerInputMessage pim = new PlayerInputMessage(); // create default so we don't get an NPE

	public RemoteInput() {

	}


	public void decodeMessage(PlayerInputMessage _pim) {
		pim = _pim;
		
		//Settings.p("Shoot dir=" + this.getDirection());

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
	public boolean isAbilityPressed(int i) {
		switch (i) {
		case 0: return pim.ability1;
		case 1: return pim.ability2;
		}
		return false;
	}

/*
	@Override
	public boolean isSelectNextAbilityPressed() {
		return pim.selectNextAbility;
	}

*/
	@Override
	public Vector3f getDirection() {
		return pim.direction;
	}


	@Override
	public Vector3f getLeft() {
		return pim.leftDir;
	}


}
