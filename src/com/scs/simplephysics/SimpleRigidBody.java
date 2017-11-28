package com.scs.simplephysics;

import java.util.Collection;

import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.collision.UnsupportedCollisionException;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class SimpleRigidBody<T> implements Collidable {

	public static final float DEFAULT_AERODYNAMICNESS = .9999f;
	public static final float DEFAULT_GRAVITY = -0.003f;

	private SimplePhysicsController<T> physicsController;
	protected Vector3f oneOffForce = new Vector3f();
	private Vector3f tmpMoveDir = new Vector3f();
	private float bounciness = .1f;
	private float aerodynamicness = DEFAULT_AERODYNAMICNESS;

	// Gravity
	private float gravInc = DEFAULT_GRAVITY; // How powerful is gravity
	private float currentGravInc = 0; // The change this frame

	private Spatial spatial;
	public T tag;
	private boolean canMove = true;
	protected boolean isOnGround = false;
	private Vector3f NO_FORCE = new Vector3f();

	private CollisionResults collisionResults = new CollisionResults();

	public SimpleRigidBody(Spatial s, SimplePhysicsController<T> _controller, T _tag) {
		super();

		spatial = s;
		physicsController = _controller;
		tag = _tag;

		physicsController.addSimpleRigidBody(this);
	}


	public void setLinearVelocity(Vector3f dir) {
		this.oneOffForce = dir;
	}


	public Vector3f getLinearVelocity() {
		return this.oneOffForce;
	}


	public void setMovable(boolean b) {
		this.canMove = b;
	}


	public void setAerodynamicness(float f) {
		this.aerodynamicness = f;
	}


	public void setGravity(float g) {
		this.gravInc = g;
	}


	public void process(float tpf_secs) {
		if (tpf_secs > 1) {
			tpf_secs = 1;
		}
		if (this.canMove) {
			Vector3f additionalForce = this.getAdditionalForce();

			// Move X
			{
				float totalOffset = oneOffForce.x + additionalForce.x;
				if (totalOffset != 0) {
					this.tmpMoveDir.set(totalOffset * tpf_secs, 0, 0);
					SimpleRigidBody<T> collidedWith = this.move(tmpMoveDir); // todo - move a max distance to prevent falling through floors
					if (collidedWith != null) {
						float bounce = this.bounciness;// * body.bounciness;
						oneOffForce.x = oneOffForce.x * bounce * -1;
					}
				}
				oneOffForce.x = oneOffForce.x * aerodynamicness;
			}

			// Move Y
			{
				//this.isOnGround = false;
				this.oneOffForce.y += currentGravInc;
				float totalOffset = oneOffForce.y + additionalForce.x;
				this.tmpMoveDir.set(0, totalOffset * tpf_secs, 0);
				SimpleRigidBody<T> collidedWith = this.move(tmpMoveDir);
				if (collidedWith != null) {
					{
						// Bounce
						float bounce = this.bounciness;
						oneOffForce.y = oneOffForce.y * bounce * -1;
						currentGravInc = currentGravInc * bounce * -1;
					}
					if (totalOffset < 0) { // Going down?
						isOnGround = true;
					}
				} else {
					// Not hit anything
					currentGravInc = currentGravInc + (gravInc * tpf_secs);
					if (totalOffset != 0) { 
						this.isOnGround = true;
					} else {
						this.isOnGround = false;
					}
				}
				//System.out.println("Is on ground: " + this.isOnGround);
				this.oneOffForce.y = oneOffForce.y * aerodynamicness;
			}

			//Move z
			{
				float totalOffset = oneOffForce.z + additionalForce.z;
				if (totalOffset != 0) {
					this.tmpMoveDir.set(0, 0, totalOffset * tpf_secs);
					SimpleRigidBody<T> collidedWith = this.move(tmpMoveDir);
					if (collidedWith != null) {
						float bounce = this.bounciness;// * body.bounciness;
						oneOffForce.z = oneOffForce.z * bounce * -1;
					}
				}
				oneOffForce.z = oneOffForce.z * aerodynamicness;
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
		Collection<SimpleRigidBody<T>> entities = physicsController.getEntities();
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


	public Vector3f getAdditionalForce() {
		return NO_FORCE; // Override if required
	}

}
