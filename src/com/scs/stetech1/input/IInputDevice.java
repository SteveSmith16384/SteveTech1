package com.scs.stetech1.input;

import com.jme3.math.Vector3f;

public interface IInputDevice {

	public abstract Vector3f getDirection();

	public abstract Vector3f getLeft();
	
	boolean getFwdValue();

	boolean getBackValue();

	boolean getStrafeLeftValue();

	boolean getStrafeRightValue();

	boolean isJumpPressed();

	boolean isShootPressed(); 

	boolean isAbilityOtherPressed();
	
	boolean isSelectNextAbilityPressed();
}
