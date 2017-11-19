package com.scs.stetech1.jme;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.objects.PhysicsRigidBody;

// https://github.com/jMonkeyEngine/jmonkeyengine/blob/master/jme3-bullet/src/common/java/com/jme3/bullet/control/BetterCharacterControl.java
public class MyBetterCharacterControl extends BetterCharacterControl {

	public MyBetterCharacterControl(float a, float b, float c) {
		super(a, b, c);
	}
	
	
	public PhysicsRigidBody getPhysicsRigidBody() {
		return super.rigidBody;
	}

}
