package com.scs.simplephysics;

/*
 * A special version of SimpleRigidBody for walking around.  
 */
public class SimpleCharacterControl<T> extends SimpleRigidBody<T> {

	//private final Vector3f walkingDir = new Vector3f();
	private float jumpForce = 5f;
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
			if (System.currentTimeMillis() - this.lastJumpTime > 1000) { // Prevent jumping again too soon
				//System.out.println("Jump!");
				this.oneOffForce.y += jumpForce;
				lastJumpTime = System.currentTimeMillis();
				return true;
			}
		}
		return false;
	}

/*
	@Override
	public Vector3f getAdditionalForce() {
		return walkingDir; // Set this to be the direction to move in.
	}
*/

}
