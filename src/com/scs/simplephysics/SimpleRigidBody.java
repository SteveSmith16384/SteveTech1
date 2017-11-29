package com.scs.simplephysics;

import java.util.Collection;

import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.collision.UnsupportedCollisionException;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class SimpleRigidBody<T> implements Collidable {

	public static final float DEFAULT_AERODYNAMICNESS = 0.99f;
	public static final float DEFAULT_GRAVITY = -4f;
	//private static Vector3f NO_FORCE = new Vector3f();

	private SimplePhysicsController<T> physicsController;
	protected Vector3f oneOffForce = new Vector3f();
	private Vector3f tmpMoveDir = new Vector3f();
	private float bounciness = .1f;
	private float aerodynamicness = DEFAULT_AERODYNAMICNESS; // 0=stops immediately, 1=goes on forever

	// Gravity
	private float gravInc = DEFAULT_GRAVITY; // How powerful is gravity
	public float currentGravInc = 0; // The change this frame

	private Spatial spatial;
	public T userObject; // Attach any object
	private boolean canMove = true; // Set to false to make "kinematic"
	protected boolean isOnGround = false;
	private Vector3f additionalForce = null;

	private CollisionResults collisionResults = new CollisionResults();

	public SimpleRigidBody(Spatial s, SimplePhysicsController<T> _controller, T _tag) {
		super();

		spatial = s;
		physicsController = _controller;
		userObject = _tag;

		physicsController.addSimpleRigidBody(this);
	}


	public void setLinearVelocity(Vector3f dir) {
		this.oneOffForce = dir;
	}

	
	public Spatial getSpatial() {
		return this.spatial;
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
			if (additionalForce == null) {
				additionalForce = Vector3f.ZERO; // Prevent NPE
			}

			// Move along X
			{
				float totalOffset = oneOffForce.x + additionalForce.x;
				if (totalOffset != 0) {
					this.tmpMoveDir.set(totalOffset * tpf_secs, 0, 0);
					SimpleRigidBody<T> collidedWith = this.move(tmpMoveDir); // todo - move a max distance to prevent falling through floors
					if (collidedWith != null) {
						float bounce = this.bounciness;// * body.bounciness; // Combine bounciness?
						oneOffForce.x = oneOffForce.x * bounce * -1;
					}
				}
				oneOffForce.x = oneOffForce.x * aerodynamicness; // Slow down
			}

			// Move along Y
			{
				//this.oneOffForce.y += currentGravInc;
				float totalOffset = (oneOffForce.y + additionalForce.y + currentGravInc) * tpf_secs;
				//totalOffset += currentGravInc;
				this.tmpMoveDir.set(0, totalOffset, 0);
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
					// No currentGravInc = currentGravInc + (gravInc);// * tpf_secs); // Fall faster
					currentGravInc = currentGravInc + (gravInc * tpf_secs); // Fall faster
					if (totalOffset != 0) { 
						this.isOnGround = true;
					} else {
						this.isOnGround = false;
					}
				}
				this.oneOffForce.y = oneOffForce.y * aerodynamicness; // Slow down
			}

			//Move along Z
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
				oneOffForce.z = oneOffForce.z * aerodynamicness; // Slow down
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
		return "SimpleRigidBody_" + userObject;
	}


	public Vector3f getAdditionalForce() {
		return additionalForce; // Override if required
	}

	
	public void setAdditionalForce(Vector3f force) {
		this.additionalForce = force; // Override if required
	}

	
	public boolean isOnGround() {
		return this.isOnGround;
	}

}

