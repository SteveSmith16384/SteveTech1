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

	private float currentGravityYChange = -.002f;
	private float gravInc = -0.002f;

	private SimplePhysicsController physicsController;
	private Vector3f moveDir = new Vector3f();
	private Vector3f tmpMoveDir = new Vector3f();
	private boolean isOnGround = false;
	private float bounciness = .5f;
	private float jumpForce = 0.1f;
	private Spatial spatial;
	public T tag;
	public boolean canMove = true;

	private CollisionResults collisionResults = new CollisionResults();

	public SimpleRigidBody(Spatial s, SimplePhysicsController _controller, T _tag) {
		super();

		spatial = s;
		physicsController = _controller;
		tag = _tag;

		physicsController.addSimpleRigidBody(this);
	}


	/*public ISimplePhysicsEntity getSimplePhysicsEntity() {
		return this.simplePhysicsEntity;
	}
	 */
	
	public void setJumpForce(float f) {
		this.jumpForce = f;
	}

	public void setLinearVelocity(Vector3f dir) {
		this.moveDir = dir;
	}


	public void setGravity(float g) {
		this.gravInc = g;
	}


	public void process(float tpf_secs) {
		if (this.canMove) {
			// Move X
			if (moveDir.x != 0) {
				moveDir.x = moveDir.x * AIR_FRICTION;
				this.tmpMoveDir.set(moveDir.x*tpf_secs, 0, 0);
				SimpleRigidBody collidedWith = this.move(tmpMoveDir); // todo - move a max distance to prevent falling through floors
				if (collidedWith != null) {
					float bounce = this.bounciness;// * body.bounciness;
					moveDir.x = moveDir.x * bounce * -1;
				}
			}

			// Move Y
			isOnGround = false;
			currentGravityYChange += gravInc;
			if (moveDir.y != 0 || currentGravityYChange != 0) {
				moveDir.y = moveDir.y * AIR_FRICTION;
				float totalOffset = moveDir.y+currentGravityYChange;
				this.tmpMoveDir.set(0, totalOffset*tpf_secs, 0);
				SimpleRigidBody collidedWith = this.move(tmpMoveDir);
				if (collidedWith != null) {
					{
						// Bounce
						float bounce = this.bounciness;
						moveDir.y = moveDir.y * bounce * -1;
						currentGravityYChange = currentGravityYChange * bounce * -1;
						//currentGravityYChange = 0; // reset gravY if not falling
					}
					if (totalOffset < 0) {
						isOnGround = true;
					}
				}

			}

			//Move z
			if (moveDir.z != 0) {
				moveDir.z = moveDir.z * AIR_FRICTION;
				this.tmpMoveDir.set(0, 0, moveDir.z*tpf_secs);
				SimpleRigidBody collidedWith = this.move(tmpMoveDir);
				if (collidedWith != null) {
					float bounce = this.bounciness;// * body.bounciness;
					moveDir.z = moveDir.z * bounce * -1;
				}
			}
		}
	}


	/*
	 * Returns object they collided with
	 */
	private SimpleRigidBody move(Vector3f offset) {
		this.spatial.move(offset);
		SimpleRigidBody wasCollision = checkForCollisions();
		if (wasCollision != null) {
			this.spatial.move(offset.negateLocal()); // Move back
		}
		return wasCollision;
	}


	/*
	 * Returns object they collided with
	 */
	public SimpleRigidBody checkForCollisions() {
		collisionResults.clear();
		SimpleRigidBody collidedWith = null;
		Collection<SimpleRigidBody> entities = physicsController.getEntities();
		synchronized (entities) {
			// Loop through the entities
			for(SimpleRigidBody e : entities) {
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


	public void jump() {
		if (isOnGround) {
			this.currentGravityYChange = jumpForce;
		}
	}


	public void setBounciness(float b) {
		this.bounciness = b;
	}


	public String toString() {
		return "SimpleRigidBody_" + tag;
	}
}
