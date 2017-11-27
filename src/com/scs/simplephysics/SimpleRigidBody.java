package com.scs.simplephysics;

import java.util.Collection;

import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.collision.UnsupportedCollisionException;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class SimpleRigidBody<T> implements Collidable {

	public static final float DEF_AIR_FRICTION = 1f; // todo re add 0.99f;
	public static final float DEF_GRAVITY = -0.0002f;

	private SimplePhysicsController physicsController;
	private Vector3f oneOffForce = new Vector3f();
	private Vector3f tmpMoveDir = new Vector3f();
	private float bounciness = .2f;
	private float airResistance = DEF_AIR_FRICTION;

	// Gravity
	private float gravInc = DEF_GRAVITY; // How powerful is gravity
	private float currentGravInc = 0; // The change this frame

	private Spatial spatial;
	public T tag;
	private boolean canMove = true;

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
		this.oneOffForce = dir;
	}


	public void setMovable(boolean b) {
		this.canMove = b;
	}


	public void setAirResistance(float f) {
		this.airResistance = f;
	}


	public void setJumpForce(float f) {
		this.jumpForce = f;
	}


	public void jump() {
		if (isOnGround) {
			this.oneOffForce.y += jumpForce;
		}
	}


	public void setGravity(float g) {
		this.gravInc = g;
	}


	public void process(float tpf_secs) {
		if (tpf_secs > 1) {
			tpf_secs = 1;
		}
		if (this.canMove) {
			// Move X
			if (oneOffForce.x != 0) {
				oneOffForce.x = oneOffForce.x * airResistance;
				this.tmpMoveDir.set(oneOffForce.x*tpf_secs, 0, 0);
				SimpleRigidBody<T> collidedWith = this.move(tmpMoveDir); // todo - move a max distance to prevent falling through floors
				if (collidedWith != null) {
					float bounce = this.bounciness;// * body.bounciness;
					oneOffForce.x = oneOffForce.x * bounce * -1;
				}
			}

			// Move Y
			isOnGround = false;
			oneOffForce.y += currentGravInc;
			oneOffForce.y = oneOffForce.y * airResistance;
			float totalOffset = oneOffForce.y;// + this.constantForce.y; // todo - copy to other axis
			//if (totalOffset != 0) {
			{
				this.tmpMoveDir.set(0, totalOffset*tpf_secs, 0);
				SimpleRigidBody<T> collidedWith = this.move(tmpMoveDir);
				if (collidedWith != null) {
					{
						// Bounce
						float bounce = this.bounciness;
						oneOffForce.y = oneOffForce.y * bounce * -1;
						currentGravInc = currentGravInc * bounce * -1; // reset gravY if not falling
					}
					if (totalOffset < 0) {
						isOnGround = true;
					}
				} else {
					currentGravInc = currentGravInc + gravInc; 
				}

			}

			//Move z
			if (oneOffForce.z != 0) {
				oneOffForce.z = oneOffForce.z * airResistance;
				this.tmpMoveDir.set(0, 0, oneOffForce.z*tpf_secs);
				SimpleRigidBody<T> collidedWith = this.move(tmpMoveDir);
				if (collidedWith != null) {
					float bounce = this.bounciness;// * body.bounciness;
					oneOffForce.z = oneOffForce.z * bounce * -1;
				}
			}
		}
	}


	/*
	 * Returns object they collided with
	 */
	private SimpleRigidBody<T> move(Vector3f offset) {
		if (offset.length() != 0) {
			this.spatial.move(offset);
			SimpleRigidBody<T> wasCollision = checkForCollisions();
			if (wasCollision != null) {
				this.spatial.move(offset.negateLocal()); // Move back
			}
			return wasCollision;
		}
		return null;
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
