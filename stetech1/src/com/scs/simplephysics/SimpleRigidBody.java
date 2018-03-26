package com.scs.simplephysics;

import java.util.List;

import com.jme3.bounding.BoundingBox;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.collision.UnsupportedCollisionException;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.scs.stevetech1.server.Globals;

public class SimpleRigidBody<T> implements Collidable {

	private SimplePhysicsController<T> physicsController;
	protected Vector3f oneOffForce = new Vector3f(); // Gets reduced by air resistance each frame
	private Vector3f tmpMoveDir = new Vector3f();
	private float bounciness = .2f;
	private float aerodynamicness; // 0=stops immediately, 1=goes on forever

	// Gravity
	private float gravInc; // How powerful is gravity for this entity
	public float currentGravInc = 0; // The y-axis change this frame caused by gravity

	//private Spatial spatial;
	public T userObject; // Attach any object - todo - remove and use ISimpleEntity
	private ISimpleEntity<T> simpleEntity;
	private boolean canMove = true; // Set to false to make "kinematic" - todo - rename?
	protected boolean isOnGround = false;
	private Vector3f additionalForce = new Vector3f(); // Additional force to apply.  Does not get changed by this code.
	private int modelComplexity = 0; // For determining which way round to check
	public boolean canWalkUpSteps = false;

	private CollisionResults collisionResults = new CollisionResults();
	private SimpleNode<T> parent;
	
	private BoundingBox bb;
	private Vector3f prevMoveDir = new Vector3f();

	public SimpleRigidBody(ISimpleEntity<T> _ent, SimplePhysicsController<T> _controller, boolean moves, T _tag) {
		super();

		simpleEntity =_ent;
		physicsController = _controller;
		this.canMove = moves;
		userObject = _tag;

		this.gravInc = physicsController.getGravity(); 
		this.aerodynamicness = physicsController.getAerodynamicness();

		//physicsController.addSimpleRigidBody(this); Don't add immediately!
	}


	public void setLinearVelocity(Vector3f dir) {
		this.oneOffForce = dir;
	}


	public Spatial getSpatial() {
		return this.simpleEntity.getSpatial();
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
		if (Globals.WARN_IF_BB_CHANGES) {
			if (bb == null) {
				bb = (BoundingBox)this.getSpatial().getWorldBound().clone();
			} else {
				BoundingBox newbb = (BoundingBox)this.getSpatial().getWorldBound();
				if (bb.getXExtent() != newbb.getXExtent() || bb.getYExtent() != newbb.getYExtent() || bb.getZExtent() != newbb.getZExtent()) {
					p("Warning - boundingbox changed size!");
					bb = (BoundingBox)newbb.clone();
				}
			}
		}
		
		if (tpf_secs > 0.1f) {
			tpf_secs = 0.1f; // Prevent stepping too far
		}
		if (this.canMove) {
			// Check we're not already colliding *before* we've even moved
			SimpleRigidBody<T> tmpWasCollision = checkForCollisions();
			if (tmpWasCollision != null) {
				System.err.println("Warning: " + this + " has collided prior to move, with " + tmpWasCollision.userObject);
				Vector3f diff = this.prevMoveDir.mult(-1);
					/*Vector3f ourPos = this.getSpatial().getWorldBound().getCenter();
					Vector3f theirPos = tmpWasCollision.getSpatial().getWorldBound().getCenter();
					Vector3f diff = ourPos.subtract(theirPos).normalizeLocal();
					if (diff.length() == 0) {
						System.err.println("No direction, moving up!");
						diff = new Vector3f(0, 1, 0);
					}*/
				do {
					this.getSpatial().move(diff); // Move away until there's no more collisions
					//System.err.println("Automoved  " + this + " by " + diff);
					tmpWasCollision = checkForCollisions();
				} while (tmpWasCollision != null);
				this.simpleEntity.hasMoved();
			}

			Vector3f additionalForce = this.getAdditionalForce();
			if (additionalForce == null) {
				additionalForce = Vector3f.ZERO; // Prevent NPE
			}

			prevMoveDir.set(oneOffForce.add(additionalForce));

			// Move along X
			{
				float totalOffset = oneOffForce.x + additionalForce.x;
				if (Math.abs(totalOffset) > SimplePhysicsController.MIN_MOVE_DIST) {
					this.tmpMoveDir.set(totalOffset * tpf_secs, 0, 0);
					SimpleRigidBody<T> collidedWith = this.move(tmpMoveDir);
					if (collidedWith != null) {
						if (!checkForStep(collidedWith, tpf_secs)) {
							float bounce = this.bounciness;// * body.bounciness; // Combine bounciness?
							oneOffForce.x = oneOffForce.x * bounce * -1;
						}
					}
				}
				oneOffForce.x = oneOffForce.x * aerodynamicness; // Slow down
			}

			//Move along Z
			{
				float totalOffset = oneOffForce.z + additionalForce.z;
				if (Math.abs(totalOffset) > SimplePhysicsController.MIN_MOVE_DIST) {
					this.tmpMoveDir.set(0, 0, totalOffset * tpf_secs);
					SimpleRigidBody<T> collidedWith = this.move(tmpMoveDir);
					if (collidedWith != null) {
						if (!checkForStep(collidedWith, tpf_secs)) {
							float bounce = this.bounciness;// * body.bounciness;
							oneOffForce.z = oneOffForce.z * bounce * -1; // Reverse direction
						}
					}
				}
				oneOffForce.z = oneOffForce.z * aerodynamicness; // Slow down
			}


			// Do Y last in case we're walking up steps
			// Move along Y
			{
				float totalOffset = (oneOffForce.y + additionalForce.y + currentGravInc);
				boolean collided = false; 
				if (Math.abs(totalOffset) > SimplePhysicsController.MIN_MOVE_DIST) {
					this.tmpMoveDir.set(0, totalOffset * tpf_secs, 0);
					SimpleRigidBody<T> collidedWith = this.move(tmpMoveDir);
					if (collidedWith != null) {
						{
							collided = true;
							// Bounce
							float bounce = this.bounciness;
							oneOffForce.y = oneOffForce.y * bounce * -1; // Reverse direction
							if (Math.abs(this.currentGravInc) > 0.5f) {
								currentGravInc = currentGravInc * bounce * -1;
							} else {
								// Avoid constantly bouncing
								currentGravInc = 0;
							}
						}
						if (totalOffset < 0) { // Going down?
							isOnGround = true;
						}
					}
				}
				if (!collided) {
					// Not hit anything
					currentGravInc = currentGravInc + (gravInc * tpf_secs); // Fall faster
					if (totalOffset != 0) { 
						this.isOnGround = true;
					} else {
						this.isOnGround = false;
					}
				}
				this.oneOffForce.y = oneOffForce.y * aerodynamicness; // Slow down
			}
		}
	}


	private boolean checkForStep(SimpleRigidBody<T> other, float tpf_secs) {
		if (!this.canWalkUpSteps) {
			return false;
		}

		BoundingBox bba = (BoundingBox)this.getSpatial().getWorldBound();
		float aBottom = bba.getCenter().y - (bba.getYExtent());
		BoundingBox bbb = (BoundingBox)other.getSpatial().getWorldBound();
		float bTop = bbb.getCenter().y + (bbb.getYExtent());
		float heightDiff = bTop - aBottom;

		if (heightDiff >= 0 && heightDiff <= 0.15f) { // todo - make a setting
			//p("Going up step: " + heightDiff);
			this.oneOffForce.y += (heightDiff / tpf_secs) / 4;
			return true;
		}


		return false;
	}


	/*
	 * Returns object they collided with.  If there are multiple collisions, it could potentially be any of them that are returned.
	 */
	private SimpleRigidBody<T> move(Vector3f offset) {
		if (offset.length() != 0) {
			if (offset.length() > SimplePhysicsController.MAX_MOVE_DIST) {
				offset.normalizeLocal().multLocal(SimplePhysicsController.MAX_MOVE_DIST);
			}
			this.getSpatial().move(offset);

			/*if (SimplePhysicsController.DEBUG) {
				if (this.getSpatial().getWorldTranslation().y <= 0) {
					p("Here");
				}
			}*/

			SimpleRigidBody<T> wasCollision = checkForCollisions();
			if (wasCollision != null) {
				this.getSpatial().move(offset.negateLocal()); // Move back

				/*if (STRICT) {
					SimpleRigidBody<T> tmpWasCollision = checkForCollisions();
					if (tmpWasCollision != null) {
						System.err.println("Warning: " + this + " has collided with " + tmpWasCollision + " even after moving back");
					}
				}*/
			} else {
				this.simpleEntity.hasMoved();
			}
			return wasCollision;
		}
		return null;
	}


	/*
	 * Returns the last object they collided with.
	 */
	public SimpleRigidBody<T> checkForCollisions() {
		SimpleRigidBody<T> collidedWith = null;
		if (SimplePhysicsController.USE_NEW_COLLISION_METHOD) {
			for(SimpleNode<T> node : this.physicsController.nodes.values()) {
				SimpleRigidBody<T> tmp = node.getCollisions(this);
				if (tmp != null) {
					collidedWith = tmp;
				}
			}
			// Check against moving entities
			List<SimpleRigidBody<T>> entities = physicsController.movingEntities;
			synchronized (entities) {
				// Loop through the entities
				for (int i=0 ; i<entities.size() ; i++) {
					SimpleRigidBody<T> e = entities.get(i);
					if (this.checkSRBvSRB(e)) {
						collidedWith = e;
					}
				}
			}


		} else {
			List<SimpleRigidBody<T>> entities = physicsController.getEntities();
			synchronized (entities) {
				// Loop through the entities
				for (int i=0 ; i<entities.size() ; i++) {
					SimpleRigidBody<T> e = entities.get(i);
					if (this.checkSRBvSRB(e)) {
						collidedWith = e;
					}
				}
			}
		}
		return collidedWith;
	}


	/**
	 * Returns whether the two SRB's collided.
	 * @param e
	 * @return
	 */
	public boolean checkSRBvSRB(SimpleRigidBody<T> e) {
		if (e != this) { // Don't check ourselves
			if (this.physicsController.getCollisionListener().canCollide(this, e)) {
				collisionResults.clear();
				// Check which object is the most complex, and collide that against the bounding box of the other
				int res = 0;
				if (this.modelComplexity >= e.modelComplexity) {
					if (e.getSpatial().getWorldBound() == null) {
						throw new RuntimeException(e.userObject + " has no bounds");
					}
					res = this.collideWith(e.getSpatial().getWorldBound(), collisionResults);
				} else {
					if (this.getSpatial().getWorldBound() == null) {
						throw new RuntimeException(this.userObject + " has no bounds");
					}
					res = e.collideWith(this.getSpatial().getWorldBound(), collisionResults);
				}
				if (res > 0) {
					this.physicsController.getCollisionListener().collisionOccurred(this, e, collisionResults.getClosestCollision().getContactPoint());
					return true;
				}
			}
		}
		return false;
	}


	@Override
	public int collideWith(Collidable other, CollisionResults results) throws UnsupportedCollisionException {
		return this.getSpatial().collideWith(other, results);
	}


	public void setBounciness(float b) {
		this.bounciness = b;
	}


	public String toString() {
		return "SimpleRigidBody_" + userObject;
	}


	/*
	 * Override if required to set additional movement force, e.g. walking.
	 */
	public Vector3f getAdditionalForce() {
		return additionalForce;
	}


	public void setAdditionalForce(Vector3f force) {
		if (!this.canMove) {
			p("Warning - setting additional force on non-moving entity");
		}
		this.additionalForce.set(force);
	}


	public boolean isOnGround() {
		return this.isOnGround;
	}


	/**
	 * When comparing for collisions, it's only possible to check BB v Mesh or BB v BB, not Mesh v Mesh.
	 * So to get the best kind of collision check, the model complexity value is used to determine which
	 * of our 2 potential colliders should be the mesh, and which should be the BB.
	 */
	public void setModelComplexity(int i) {
		this.modelComplexity = i;
	}


	public boolean canMove() {
		return this.canMove;
	}


	public void removeFromParent() {
		if (this.parent != null) {
			this.parent.remove(this);
			if (this.parent.getNumChildren() == 0) {
				this.physicsController.nodes.remove(this.parent.id);
			}
		}
	}


	public static void p(String s) {
		System.out.println(s);
	}


}

