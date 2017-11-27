package com.scs.simplephysics;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/*
 * Need this for walkDir
 */
public class SimpleCharacterControl<T> extends SimpleRigidBody<T> {
	
	private Vector3f walkDir;
	private float jumpForce = 0.1f;

	public SimpleCharacterControl(Spatial s, SimplePhysicsController<T> _controller, T _tag) {
		super(s, _controller, _tag);
	}
	

	public void setJumpForce(float f) {
		this.jumpForce = f;
	}


	public void jump() {
		if (isOnGround) {
			this.oneOffForce.y += jumpForce;
		}
	}


	public Vector3f getAdditionalForce() {
		return walkDir;
	}
	

}
