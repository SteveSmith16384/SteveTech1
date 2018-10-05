package com.scs.simplephysics;

/*
 * A special version of SimpleRigidBody for walking around.  
 */
public class SimpleCharacterControl<T> extends SimpleRigidBody<T> {

	private float jumpForce = 7f;//5f;
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
	public void process(float tpf_secs) {
		super.process(tpf_secs);
		
		//p("Is on ground = " + this.isOnGround);
	}
	
	
/*
	@Override
	public Vector3f getAdditionalForce() {
		return walkingDir; // Set this to be the direction to move in.
	}
*/

}
