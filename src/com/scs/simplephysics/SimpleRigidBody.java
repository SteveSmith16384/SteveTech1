package com.scs.simplephysics;

import java.util.Collection;

import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.collision.UnsupportedCollisionException;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class SimpleRigidBody implements Collidable {

	private static final float AIR_FRICTION = 0.99f;
	//private static final Vector3f DOWN = new Vector3f(0, -1, 0);

	private float currentGravityYChange = -.02f; // todo - inc if falling
	private float gravInc = 0.02f;

	private SimplePhysicsController physicsController;
	private Vector3f moveDir = new Vector3f();
	private Vector3f tmpMoveDir = new Vector3f();
	private boolean isOnGround = false;
	private float bounciness = .5f;
	private Spatial spatial;
	public Object tag;

	private CollisionResults collisionResults = new CollisionResults();

	public SimpleRigidBody(Spatial s, SimplePhysicsController _controller, Object _tag) {
		super();

		spatial = s;
		//simplePhysicsEntity = _entity;
		physicsController = _controller;
		tag = _tag;
	}


	/*public ISimplePhysicsEntity getSimplePhysicsEntity() {
		return this.simplePhysicsEntity;
	}
	 */

	public void setLinearVelocity(Vector3f dir) {
		this.moveDir = dir;
	}


	public void setGravity(float g) {
		this.gravInc = g;
	}

	public void process(float tpf_secs) {
		// Move X
		if (moveDir.x != 0) {
			moveDir.x = moveDir.x * AIR_FRICTION;
			this.tmpMoveDir.set(moveDir.x, 0, 0);
			SimpleRigidBody collidedWith = this.move(tmpMoveDir);
			if (collidedWith != null) {
				//SimpleRigidBody body = collidedWith.getSimpleRigidBody();
				float bounce = this.bounciness;// * body.bounciness;
				moveDir.x = moveDir.x * bounce;
			}
		}
		// Move Y
		isOnGround = false;
		if (gravInc != 0) {
			currentGravityYChange -= gravInc;
		}
		if (moveDir.y != 0 || currentGravityYChange != 0) {
			moveDir.y = moveDir.y * AIR_FRICTION;
			float totalOffset = moveDir.y+currentGravityYChange;
			this.tmpMoveDir.set(0, totalOffset, 0);
			SimpleRigidBody collidedWith = this.move(tmpMoveDir);
			if (collidedWith != null) {
				//SimpleRigidBody body = collidedWith.getSimpleRigidBody();
				float bounce = this.bounciness;// * body.bounciness;
				moveDir.y = moveDir.y * bounce;
				currentGravityYChange = 0; // reset gravY if not falling
				if (totalOffset < 0) {
					isOnGround = true;
				}
			}

		}

		//Move z
		if (moveDir.z != 0) {
			moveDir.z = moveDir.z * AIR_FRICTION;
			this.tmpMoveDir.set(0, 0, moveDir.z);
			SimpleRigidBody collidedWith = this.move(tmpMoveDir);
			if (collidedWith != null) {
				//SimpleRigidBody body = collidedWith.getSimpleRigidBody();
				float bounce = this.bounciness;// * body.bounciness;
				moveDir.z = moveDir.z * bounce;
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
					if (this.collideWith(spatial.getWorldBound(), collisionResults) > 0) {
						collidedWith = e;
						this.physicsController.collListener.collisionOccurred(this, e, collisionResults.getClosestCollision().getContactPoint());
						collisionResults.clear();
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
			this.currentGravityYChange = 1f;
		}
	}


	public void setBounciness(float b) {
		this.bounciness = b;
	}
}
