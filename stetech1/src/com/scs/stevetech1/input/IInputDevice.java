package com.scs.stevetech1.input;

import com.jme3.math.Vector3f;

public interface IInputDevice {

	public abstract Vector3f getDirection();

	public abstract Vector3f getLeft();
	
	boolean getFwdValue();

	boolean getBackValue();

	boolean getStrafeLeftValue();

	boolean getStrafeRightValue();

	boolean isJumpPressed();

	boolean isAbilityPressed(int i);
	
	//void reset();
}
