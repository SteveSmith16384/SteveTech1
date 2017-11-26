package com.scs.simplephysics;

import java.util.Collection;

import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.collision.UnsupportedCollisionException;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class SimpleRigidBody<T> implements Collidable {

	private static final float AIR_FRICTION = 0.99f;
	//private static final Vector3f DOWN = new Vector3f(0, -1, 0);

	//private float currentGravityYChange = -.002f;
	private float gravInc = -0.002f;
	public Vector3f otherForce = new Vector3f(); // caught in explosion etc...

	private SimplePhysicsController physicsController;
	private Vector3f constantForce = new Vector3f();
	private Vector3f tmpMoveDir = new Vector3f();
	private float bounciness = .5f;
	private Spatial spatial;
	public T tag;
	public boolean canMove = true;
	
	private float jumpForce = 0.1f;
	private boolean isOnGround = false;


	private CollisionResults collisionResults = new CollisionResults();

	public SimpleRigidBody(Spatial s, SimplePhysicsController _controller, T _tag) {
		super();

		spatial = s;
		physicsController = _controller;
		tag = _tag;

		physicsController.addSimpleRigidBody(this);
	}


	public void setLinearVelocity(Vector3f dir) {
		this.constantForce = dir;
	}


	public void setJumpForce(float f) {
		this.jumpForce = f;
	}


	public void jump() {
		if (isOnGround) {
			this.otherForce.y += jumpForce;
		}
	}


	public void setGravity(float g) {
		this.gravInc = g;
	}


	public void process(float tpf_secs) {
		if (this.canMove) {
			// Move X
			if (constantForce.x != 0) {
				constantForce.x = constantForce.x * AIR_FRICTION;
				this.tmpMoveDir.set(constantForce.x*tpf_secs, 0, 0);
				SimpleRigidBody<T> collidedWith = this.move(tmpMoveDir); // todo - move a max distance to prevent falling through floors
				if (collidedWith != null) {
					float bounce = this.bounciness;// * body.bounciness;
					constantForce.x = constantForce.x * bounce * -1;
				}
			}

			// Move Y
			isOnGround = false;
			otherForce.y += gravInc;
			constantForce.y = constantForce.y * AIR_FRICTION;
			float totalOffset = constantForce.y + this.otherForce.y; // todo - copy to other axis
			if (totalOffset != 0) {
				this.tmpMoveDir.set(0, totalOffset*tpf_secs, 0);
				SimpleRigidBody<T> collidedWith = this.move(tmpMoveDir);
				if (collidedWith != null) {
					{
						// Bounce
						float bounce = this.bounciness;
						constantForce.y = constantForce.y * bounce * -1;
						otherForce.y = otherForce.y * bounce * -1;
						//currentGravityYChange = 0; // reset gravY if not falling
					}
					if (totalOffset < 0) {
						isOnGround = true;
					}
				}

			}

			//Move z
			if (constantForce.z != 0) {
				constantForce.z = constantForce.z * AIR_FRICTION;
				this.tmpMoveDir.set(0, 0, constantForce.z*tpf_secs);
				SimpleRigidBody<T> collidedWith = this.move(tmpMoveDir);
				if (collidedWith != null) {
					float bounce = this.bounciness;// * body.bounciness;
					constantForce.z = constantForce.z * bounce * -1;
				}
			}
		}
	}


	/*
	 * Returns object they collided with
	 */
	private SimpleRigidBody<T> move(Vector3f offset) {
		this.spatial.move(offset);
		SimpleRigidBody<T> wasCollision = checkForCollisions();
		if (wasCollision != null) {
			this.spatial.move(offset.negateLocal()); // Move back
		}
		return wasCollision;
	}


	/*
	 * Returns object they collided with
	 */
	public SimpleRigidBody<T> checkForCollisions() {
		collisionResults.clear();
		SimpleRigidBody<T> collidedWith = null;
		Collection<SimpleRigidBody> entities = physicsController.getEntities();
		synchronized (entities) {
			// Loop through the entities
			for(SimpleRigidBody<T> e : entities) {
				if (e != this) { // Don't check ourselves
					if (this.physicsController.getCollisionListener().canCollide(this, e)) {
						if (this.collideWith(e.spatial.getWorldBound(), collisionResults) > 0) {
							collidedWith = e;
							this.physicsController.getCollisionListener().collisionOccurred(this, e, collisionResults.getClosestCollision().getContactPoint());
							collisionResults.clear();
						}
					}
				}
			}
		}
		return collidedWith;
	}


	@Override
	public int collideWith(Collidable other, CollisionResults results) throws UnsupportedCollisionException {
		return this.spatial.collideWith(other, results);
	}


	public void setBounciness(float b) {
		this.bounciness = b;
	}


	public String toString() {
		return "SimpleRigidBody_" + tag;
	}
}
