package com.scs.simplephysics;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/*
 * Need this for walkDir
 */
public class SimpleCharacterControl<T> extends SimpleRigidBody<T> {
	
	private Vector3f walkDir = new Vector3f();
	private float jumpForce = 18f;

	public SimpleCharacterControl(Spatial s, SimplePhysicsController<T> _controller, T _tag) {
		super(s, _controller, _tag);
		
		this.setBounciness(0);
		this.setAerodynamicness(1);
	}
	

	public void setJumpForce(float f) {
		this.jumpForce = f;
	}


	public void jump() {
		if (isOnGround) {
			System.out.println("Jump!");
			this.oneOffForce.y += jumpForce;
		}
	}


	public Vector3f getAdditionalForce() {
		walkDir.y = 0;
		return walkDir;
	}
	

}
