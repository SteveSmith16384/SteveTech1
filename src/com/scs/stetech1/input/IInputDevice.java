package com.scs.stetech1.input;

public interface IInputDevice {

	boolean getFwdValue();

	boolean getBackValue();

	boolean getStrafeLeftValue();

	boolean getStrafeRightValue();

	boolean isJumpPressed();

	boolean isShootPressed();

	boolean isAbilityOtherPressed();
	
	boolean isSelectNextAbilityPressed();

}
