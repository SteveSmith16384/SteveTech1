package com.scs.stetech1.input;
/*
import com.jme3.input.InputManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.scs.stetech1.netmessages.PlayerInputMessage;

public class CombinedInputMethod extends MouseAndKeyboardCamera {

	private PlayerInputMessage pim = new PlayerInputMessage(); // create default so we don't get an NPE

	public CombinedInputMethod(Camera cam, InputManager _inputManager) {
		super(cam, _inputManager);
	}


	public void decodeMessage(PlayerInputMessage _pim) {
		pim = _pim;

	}

	@Override
	public boolean getFwdValue() {
		return super.getFwdValue() || pim.fwd;
	}

	@Override
	public boolean getBackValue() {
		return super.getBackValue() || pim.back;
	}

	@Override
	public boolean getStrafeLeftValue() {
		return super.getStrafeLeftValue() || pim.strafeLeft;
	}

	@Override
	public boolean getStrafeRightValue() {
		return super.getStrafeRightValue() || pim.strafeRight;
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


	/*@Override
	public Vector3f getDirection() {
		return pim.direction;
	}


	@Override
	public Vector3f getLeft() {
		return pim.leftDir;
	}*/


//}
