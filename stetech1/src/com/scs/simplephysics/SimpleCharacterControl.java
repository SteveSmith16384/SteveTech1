package com.scs.simplephysics;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/*
 * Need this for walkDir
 */
public class SimpleCharacterControl<T> extends SimpleRigidBody<T> {

	private final Vector3f additionalMoveDir = new Vector3f();
	private float jumpForce = 5f;
	private long lastJumpTime = 0;

	public SimpleCharacterControl(Spatial s, SimplePhysicsController<T> _controller, T _tag) {
		super(s, _controller, true, _tag);

		this.setBounciness(0);
		//this.setAerodynamicness(1);  Don't set to 1, otherwise an explosion will keep moving us forever
		//super.setAdditionalForce(walkDir);
	}


	public void setJumpForce(float f) {
		this.jumpForce = f;
	}


	public boolean jump() {
		if (isOnGround) {
			if (System.currentTimeMillis() - this.lastJumpTime > 1000) { // Prevent jumping again too soon
				//System.out.println("Jump!");
				this.oneOffForce.y += jumpForce;
				lastJumpTime = System.currentTimeMillis();
				return true;
			}
		}
		return false;
	}


	@Override
	public Vector3f getAdditionalForce() {
		return additionalMoveDir; // Set this to be the direction to move in.
	}


}
