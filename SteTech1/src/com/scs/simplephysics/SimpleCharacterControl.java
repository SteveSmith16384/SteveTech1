package com.scs.simplephysics;

import com.scs.stevetech1.server.Globals;

/*
 * A special version of SimpleRigidBody for walking around.  
 */
public class SimpleCharacterControl<T> extends SimpleRigidBody<T> {

	private float jumpForce = 7f;
	private long lastJumpTime = 0;

	public SimpleCharacterControl(ISimpleEntity<T> s, SimplePhysicsController<T> _controller, T _tag) {
		super(s, _controller, true, _tag);

		this.setBounciness(0);
		this.canWalkUpSteps = true;
	}


	public void setJumpForce(float f) {
		this.jumpForce = f;
	}


	public boolean jump() {
		if (isOnGround) {
			if (System.currentTimeMillis() - this.lastJumpTime > 1000) { // Prevent jumping again too soon.  Todo - make config
				if (Globals.DEBUG_JUMPSYNC_PROBLEM) {
					Globals.p("Jump!  Force=" + jumpForce + " Grav=" + this.getGravity());
				}
				this.oneOffForce.y += jumpForce;
				lastJumpTime = System.currentTimeMillis();
				return true;
			}
		}
		return false;
	}

}
