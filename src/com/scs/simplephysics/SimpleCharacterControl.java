package com.scs.simplephysics;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/*
 * Need this for walkDir
 */
public class SimpleCharacterControl<T> extends SimpleRigidBody<T> {

	private Vector3f walkDir = new Vector3f();
	private float jumpForce = 9f;
	private long lastJumpTime = 0;

	public SimpleCharacterControl(Spatial s, SimplePhysicsController<T> _controller, T _tag) {
		super(s, _controller, _tag);

		this.setBounciness(0);
		//this.setAerodynamicness(1);  Don't set to 1, otherwise an explosions keeps moving us forever
	}


	public void setJumpForce(float f) {
		this.jumpForce = f;
	}


	public void jump() {
		if (isOnGround) {
			if (System.currentTimeMillis() - this.lastJumpTime > 1000) { // Prevent jumping again too soon
				System.out.println("Jump!");
				this.oneOffForce.y += jumpForce;
				lastJumpTime = System.currentTimeMillis();
			}
		}
	}


	public Vector3f getAdditionalForce() {
		return walkDir; // Set this to be the direction to walk in.
	}


}
