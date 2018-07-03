package com.scs.simplephysics;

import java.util.ArrayList;
import java.util.List;

import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.collision.UnsupportedCollisionException;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.scs.stevetech1.server.Globals;

public class SimpleRigidBody<T> implements Collidable {

	private static final boolean AUTOMOVE_BY_FORCE = false; // Doesn't work since move() moves entity back to original pos on collision
	private static final boolean DEBUG_AUTOMOVING = true;

	private static final float AUTOMOVE_FRAC = .1f;
	private static final float MAX_STEP_HEIGHT = 0.3f;//0.25f; // todo - make config
	private static final Vector3f DOWN_VEC = new Vector3f(0, -1, 0);

	private SimplePhysicsController<T> physicsController;
	protected Vector3f oneOffForce = new Vector3f(); // e.g. force of explosion.  Gets reduced by air resistance each frame, and reversed if bounced
	protected Vector3f additionalForce = new Vector3f(); // Additional force to apply, e.g. walking force or jet.  Does not get changed by this code.
	protected Vector3f automoveForce = new Vector3f(); // Additional force to apply to move an entity "out" of another entity

	private Vector3f tmpMoveDir = new Vector3f();
	private float bounciness = .2f;
	private float aerodynamicness; // 0=stops immediately, 1=goes on forever

	// Gravity
	private float gravInc; // How powerful is gravity for this entity
	private float currentGravInc = 0; // The y-axis change this frame caused by gravity

	public T userObject; // Attach any object - todo - remove and use ISimpleEntity
	public ISimpleEntity<T> simpleEntity;
	private boolean movedByForces = true; // Set to false to make "kinematic"
	protected boolean isOnGround = false;
	private int modelComplexity = 0; // For determining which way round to check collisions
	public boolean canWalkUpSteps = false;

	private SimpleNode<T> parent;

	public boolean removed = false;
	private boolean neverMoves = false; // More efficient if true
	private Vector3f tmpPrevPos = new Vector3f();
	private boolean collidable = true;

	public SimpleRigidBody(ISimpleEntity<T> _ent, SimplePhysicsController<T> _controller, boolean _movedByForces, T _tag) {
		super();

		simpleEntity =_ent;
		physicsController = _controller;
		this.movedByForces = _movedByForces;
		userObject = _tag;

		this.gravInc = physicsController.getGravity(); 
		this.aerodynamicness = physicsController.getAerodynamicness();

	}


	public void setLinearVelocity(Vector3f dir) {
		this.oneOffForce = dir;
	}


	/**
	 * Helper.  Need this to get overall dimensions and position of entity.
	 * @return
	 */
	public BoundingBox getBoundingBox() {
		Collidable c = this.simpleEntity.getCollidable();
		if (c instanceof BoundingBox) {
			return (BoundingBox)c;
		} else {
			Spatial s = (Spatial)c;
			return (BoundingBox)s.getWorldBound();
		}
	}


	public Vector3f getLinearVelocity() {
		return this.oneOffForce;
	}


	public void setMovedByForces(boolean b) {
		this.movedByForces = b;
	}


	public void setAerodynamicness(float f) {
		this.aerodynamicness = f;
	}


	public void setGravity(float g) {
		this.gravInc = g;
	}


	public void process(float tpf_secs) {
		if (tpf_secs > 0.1f) {
			tpf_secs = 0.1f; // Prevent stepping too far
		}

		if (this.movedByForces) {
			automoveForce.set(0, 0, 0);

			// Check we're not already colliding *before* we've even moved
			boolean doBounce = true;
			List<SimpleRigidBody<T>> crs = this.checkForCollisions();
			if (crs.size() != 0) {
				System.err.println("Warning: " + this + " has collided prior to move, with " + crs.toString());
				if (AUTOMOVE_BY_FORCE) {
					doBounce = false; // Don't bounce if we're being extracted from another entity
					this.applyForceAwayFrom(crs);
				} else {
					this.moveAwayFrom(crs);
					return; // Don't bother moving any more!
				}
			}

			// Move along X
			{
				float totalOffset = oneOffForce.x + additionalForce.x + this.automoveForce.x;
				if (Math.abs(totalOffset) > SimplePhysicsController.MIN_MOVE_DIST) {
					this.tmpMoveDir.set(totalOffset * tpf_secs, 0, 0);
					tmpPrevPos.set(this.getBoundingBox().getCenter());
					List<SimpleRigidBody<T>> crs2 = this.move(tmpMoveDir);
					if (crs2.size() > 0) {
						if (!checkForStep(crs2, tmpPrevPos, tmpMoveDir)) {
							if (doBounce) {
								float bounce = this.bounciness;// * body.bounciness; // Combine bounciness?
								oneOffForce.x = oneOffForce.x * bounce * -1;
							}
						}
					}
				}
				oneOffForce.x = oneOffForce.x * aerodynamicness; // Slow down
			}

			//Move along Z
			{
				float totalOffset = oneOffForce.z + additionalForce.z + this.automoveForce.z;
				if (Math.abs(totalOffset) > SimplePhysicsController.MIN_MOVE_DIST) {
					this.tmpMoveDir.set(0, 0, totalOffset * tpf_secs);
					tmpPrevPos.set(this.getBoundingBox().getCenter());
					List<SimpleRigidBody<T>> crs2 = this.move(tmpMoveDir);
					if (crs2.size() > 0) {
						if (!checkForStep(crs2, tmpPrevPos, this.tmpMoveDir)) {
							if (doBounce) {
								float bounce = this.bounciness;// * body.bounciness;
								oneOffForce.z = oneOffForce.z * bounce * -1; // Reverse direction
							}
						}
					}
				}
				oneOffForce.z = oneOffForce.z * aerodynamicness; // Slow down
			}


			// Do Y last in case we're walking up steps
			// Move along Y
			{
				float totalOffset = (oneOffForce.y + additionalForce.y + this.automoveForce.y + currentGravInc);
				boolean collided = false;
				if (Math.abs(totalOffset) > SimplePhysicsController.MIN_MOVE_DIST) {
					isOnGround = false;
					this.tmpMoveDir.set(0, totalOffset * tpf_secs, 0);
					List<SimpleRigidBody<T>> crs2 = this.move(tmpMoveDir);
					if (crs2.size() > 0) {
						collided = true;
						if (doBounce) {
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
					} else {
						// Not hit anything
						/*if (totalOffset != 0) { 
							this.isOnGround = true;
						} else {
							this.isOnGround = false;
						}*/

					}
				}
				if (!collided) {
					currentGravInc = currentGravInc + (gravInc * tpf_secs); // Fall faster
				}
				this.oneOffForce.y = oneOffForce.y * aerodynamicness; // Slow down
			}

			/*if (this.currentGravInc < GRAVITY_WARNING) {
				p("Warning - high gravity offset: " + this.currentGravInc);
			}*/
		}
	}


	private void moveAwayFrom(List<SimpleRigidBody<T>> others) {
		BoundingBox ourBB = this.getBoundingBox();
		for (SimpleRigidBody<T> otherSRB : others) {
			if (otherSRB.simpleEntity.getCollidable() instanceof TerrainQuad) {
				Vector3f start = ourBB.getCenter().clone();
				start.y = 255f;
				Ray ray = new Ray(start, DOWN_VEC);
				CollisionResults crs2 = new CollisionResults();
				if (otherSRB.collideWith(ray, crs2) > 0) {
					Vector3f diff = crs2.getClosestCollision().getContactPoint();
					diff.x = 0;
					diff.y = diff.y - (ourBB.getCenter().y - ourBB.getYExtent());
					diff.z = 0;
					if (DEBUG_AUTOMOVING) {
						p("Automoved  " + this + " by " + diff);
					}
					this.simpleEntity.moveEntity(diff);
				}

			} else {
				BoundingBox theirBB = otherSRB.getBoundingBox();

				// X axis
				boolean doX = true;
				if (ourBB.getCenter().x - ourBB.getXExtent() > theirBB.getCenter().x - theirBB.getXExtent()) {
					if (ourBB.getCenter().x + ourBB.getXExtent() < theirBB.getCenter().x + theirBB.getXExtent()) {
						doX = false;
					}
				}
				if (doX) {
					float len = ourBB.getCenter().x - theirBB.getCenter().x; //todo - adjust by diff of edges!
					Vector3f diff = new Vector3f(len*AUTOMOVE_FRAC, 0, 0);
					if (DEBUG_AUTOMOVING) {
						p("Automoved  " + this + " by " + diff);
					}
					this.simpleEntity.moveEntity(diff); // Move away
				}

				// Z axis
				boolean doZ = true;
				if (ourBB.getCenter().z - ourBB.getZExtent() > theirBB.getCenter().z - theirBB.getZExtent()) {
					if (ourBB.getCenter().z + ourBB.getZExtent() < theirBB.getCenter().z + theirBB.getZExtent()) {
						doZ = false;
					}
				}
				if (doZ) {
					float len = ourBB.getCenter().z - theirBB.getCenter().z;
					Vector3f diff = new Vector3f(0, 0, len*AUTOMOVE_FRAC);
					if (DEBUG_AUTOMOVING) {
						p("Automoved  " + this + " by " + diff);
					}
					this.simpleEntity.moveEntity(diff); // Move away
				}

				// Y axis
				boolean doY = true;
				if (ourBB.getCenter().y - ourBB.getYExtent() > theirBB.getCenter().y - theirBB.getYExtent()) {
					if (ourBB.getCenter().y + ourBB.getYExtent() < theirBB.getCenter().y + theirBB.getYExtent()) {
						doY = false;
					}
				}
				if (doY) {
					float len = ourBB.getCenter().y - theirBB.getCenter().y;
					Vector3f diff = new Vector3f(0, len*AUTOMOVE_FRAC, 0);
					if (DEBUG_AUTOMOVING) {
						p("Automoved  " + this + " by " + diff);
					}
					this.simpleEntity.moveEntity(diff); // Move away
				}
			}
		}
	}


	private void applyForceAwayFrom(List<SimpleRigidBody<T>> others) {
		BoundingBox ourBB = this.getBoundingBox();
		for (SimpleRigidBody<T> otherSRB : others) {
			if (otherSRB.simpleEntity.getCollidable() instanceof TerrainQuad) {
				this.automoveForce.y += AUTOMOVE_FRAC;
				if (DEBUG_AUTOMOVING) {
					p("Automoved  " + this + " by " + automoveForce);
				}
			} else {
				BoundingBox theirBB = otherSRB.getBoundingBox();

				// X axis
				boolean doX = true;
				if (ourBB.getCenter().x - ourBB.getXExtent() > theirBB.getCenter().x - theirBB.getXExtent()) {
					if (ourBB.getCenter().x + ourBB.getXExtent() < theirBB.getCenter().x + theirBB.getXExtent()) {
						doX = false;
					}
				}
				if (doX) {
					float len = ourBB.getCenter().x - theirBB.getCenter().x; //todo - adjust by diff of edges!
					if (len != 0) {
						this.automoveForce.x += len*AUTOMOVE_FRAC;
						if (DEBUG_AUTOMOVING) {
							p("Automoved  " + this + " by " + automoveForce);
						}
					}
				}

				// Z axis
				boolean doZ = true;
				if (ourBB.getCenter().z - ourBB.getZExtent() > theirBB.getCenter().z - theirBB.getZExtent()) {
					if (ourBB.getCenter().z + ourBB.getZExtent() < theirBB.getCenter().z + theirBB.getZExtent()) {
						doZ = false;
					}
				}
				if (doZ) {
					float len = ourBB.getCenter().z - theirBB.getCenter().z;
					if (len != 0) {
						this.automoveForce.z += len*AUTOMOVE_FRAC;
						if (DEBUG_AUTOMOVING) {
							p("Automoved  " + this + " by " + automoveForce);
						}
					}
				}

				// Y axis
				boolean doY = true;
				if (ourBB.getCenter().y - ourBB.getYExtent() > theirBB.getCenter().y - theirBB.getYExtent()) {
					if (ourBB.getCenter().y + ourBB.getYExtent() < theirBB.getCenter().y + theirBB.getYExtent()) {
						doY = false;
					}
				}
				if (doY) {
					float len = ourBB.getCenter().y - theirBB.getCenter().y;
					if (len != 0) {
						automoveForce.y += len*AUTOMOVE_FRAC;
						if (DEBUG_AUTOMOVING) {
							p("Automoved  " + this + " by " + automoveForce);
						}
					}
				}
			}
		}
	}


	/**
	 * Move the player up slightly, so they can walk up a step (if it is low enough).
	 * 
	 * @return
	 */
	private boolean checkForStep(List<SimpleRigidBody<T>> crs, Vector3f posBeforeMove, Vector3f moveOffset) {
		if (!this.canWalkUpSteps) {
			return false;
		}

		if (!this.isOnGround) {
			return false;
		}

		SimpleRigidBody<T> cr = crs.get(0);

		BoundingBox ourBB = (BoundingBox)this.getBoundingBox();
		float ourHeight = ourBB.getCenter().y - ourBB.getYExtent();

		// Check for stairs first.  treat stairs and slopes differently since we handle the upward force differently
		if (cr.simpleEntity.getCollidable() instanceof TerrainQuad == false && cr.simpleEntity.getCollidable() instanceof Geometry == false) {
			Globals.p("Checking for stairs");
			// Check simple BB against simple BB 
			BoundingBox theirBB = (BoundingBox)cr.getBoundingBox(); // cr.simpleEntity.getCollidable();
			float nextHeight = theirBB.getCenter().y + (theirBB.getYExtent());
			float heightDiff = nextHeight - ourHeight;

			if (heightDiff > 0 && heightDiff <= MAX_STEP_HEIGHT) {
				p("Going up step: height=" + heightDiff + ", stepForce=" + this.physicsController.getStepForce());
				this.oneOffForce.y += Math.sqrt(heightDiff) * 8f; // 10f;//14f;//16f; // this.physicsController.getStepForce();
				return true;
			} else {
				p("NOT Going up step: height=" + heightDiff);
			}

		} else {
			Globals.p("Checking for slope");

			float DEF_EXTENT = 0.1f;
			float extent = ourBB.getXExtent() + DEF_EXTENT;

			CollisionResults rayCRs = new CollisionResults();
			Vector3f newPos = posBeforeMove.add(moveOffset.normalize().mult(extent)); // todo - don't create new each time
			Ray nextRay = new Ray(newPos, DOWN_VEC);
			rayCRs.clear();
			cr.simpleEntity.getCollidable().collideWith(nextRay, rayCRs);
			if (rayCRs.size() > 0) { 
				float nextHeight = rayCRs.getClosestCollision().getContactPoint().y;
				float heightDiff = nextHeight - ourHeight;
				float ratio = heightDiff / DEF_EXTENT; // <1 = 45 degrees or walkable
				if (heightDiff > 0 && heightDiff <= MAX_STEP_HEIGHT && ratio <= 1f) {
					p("Walking up ramp! heightDiff=" + heightDiff + "; ratio= " + ratio);
					this.oneOffForce.y += Math.sqrt(ratio) * 3f;//4f; // 6f;//this.physicsController.getRampForce(); //  Adjust by steepness
					return true;
				} else {
					p("NOT Going up step: heightDiff=" + heightDiff);
				}
			} else {
				// Will be empty if we're walking OFF a ramp
			}
		}
		return false;
	}


	/*
	 * Moves and entity and returns object they collided with.
	 */
	private List<SimpleRigidBody<T>> move(Vector3f offset) {
		this.simpleEntity.moveEntity(offset);
		List<SimpleRigidBody<T>> crs = checkForCollisions();
		if (crs.size() > 0) {
			this.simpleEntity.moveEntity(offset.negate()); // Move back
		}
		return crs;
	}


	public List<SimpleRigidBody<T>> checkForCollisions() {
		List<SimpleRigidBody<T>> crs = new ArrayList<SimpleRigidBody<T>>();

		if (!this.collidable ) {
			return crs;
		}

		CollisionResults tempCollisionResults = new CollisionResults(); // Avoid creating a new one each time

		for(SimpleNode<T> node : this.physicsController.nodes.values()) {
			node.getCollisions(this, crs, tempCollisionResults);
		}
		// Check against moving/big entities
		List<SimpleRigidBody<T>> entities = physicsController.movingEntities;
		synchronized (entities) {
			// Loop through the entities
			for (int i=0 ; i<entities.size() ; i++) {
				SimpleRigidBody<T> e = entities.get(i);
				if (this.checkSRBvSRB(e, tempCollisionResults)) {
					crs.add(e);
				}
			}
		}
		return crs;
	}


	/**
	 * Returns whether the two SRB's collided.
	 * @param e
	 * @return
	 */
	public boolean checkSRBvSRB(SimpleRigidBody<T> e, CollisionResults tempCollisionResults) {
		tempCollisionResults.clear();
		if (e != this) { // Don't check ourselves
			if (this.physicsController.getCollisionListener().canCollide(this, e)) {
				// Check which object is the most complex, and collide that against the bounding box of the other
				int res = 0;
				if (this.simpleEntity.getCollidable() instanceof TerrainQuad || e.simpleEntity.getCollidable() instanceof TerrainQuad) {
					TerrainQuad tq = null;
					BoundingBox bv = null;
					if (this.simpleEntity.getCollidable() instanceof TerrainQuad) {
						tq = (TerrainQuad)this.simpleEntity.getCollidable();
						bv = e.getBoundingBox();
					} else {
						tq = (TerrainQuad)e.simpleEntity.getCollidable();
						bv = this.getBoundingBox();
					}
					Vector3f start = bv.getCenter().clone();
					start.y = 255f;
					Ray ray = new Ray(start, DOWN_VEC);
					//ray.setLimit(bv.getYExtent());
					res = tq.collideWith(ray, tempCollisionResults);
					if (res > 0) {
						// Compare positions
						Vector3f pos = tempCollisionResults.getClosestCollision().getContactPoint();
						if (bv.getCenter().y-bv.getYExtent() >= pos.y) {
							return false;
						} else {
							p("Hit terrain");
						}
					}

				} else if (this.simpleEntity.getCollidable() instanceof BoundingVolume == false && e.simpleEntity.getCollidable() instanceof BoundingVolume == false) {
					// Both are complex meshes!  Convert one into a simple boundingvolume
					if (this.modelComplexity >= e.modelComplexity) {
						// We are the most complex
						Node s = (Node)e.simpleEntity.getCollidable();
						BoundingVolume bv = (BoundingVolume)s.getWorldBound();
						res = this.collideWith(bv, tempCollisionResults);
					} else {
						// They are the most complex
						Node s = (Node)this.simpleEntity.getCollidable();
						BoundingVolume bv = (BoundingVolume)s.getWorldBound();
						res = bv.collideWith(e.simpleEntity.getCollidable(), tempCollisionResults);
						//res = e.collideWith(this.simpleEntity.getCollidable(), tempCollisionResults);
					}


				} else {
					res = this.collideWith(e.simpleEntity.getCollidable(), tempCollisionResults);

				}
				/*if (this.modelComplexity >= e.modelComplexity) {
					if (e.getBoundingBox() == null) {
						throw new RuntimeException(e.userObject + " has no bounds");
					}
					res = this.collideWith(e.simpleEntity.getCollidable(), tempCollisionResults);
				} else {
					if (this.getBoundingBox() == null) {
						throw new RuntimeException(this.userObject + " has no bounds");
					}
					res = e.collideWith(this.simpleEntity.getCollidable(), tempCollisionResults);
				}*/
				if (res > 0) {
					this.physicsController.getCollisionListener().collisionOccurred(this, e);
					return true;
				}
			}
		}
		return false;
	}


	/**
	 * If you get UnsupportedCollisionException, it means you're trying to collide a mesh with a mesh.
	 */
	@Override
	public int collideWith(Collidable other, CollisionResults results) throws UnsupportedCollisionException {
		//try {
		return this.simpleEntity.getCollidable().collideWith(other, results);
		/*} catch (UnsupportedCollisionException ex) {
			ex.printStackTrace();
			this.simpleEntity.getCollidable().collideWith(other, results);
		}
		return 0;*/
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
		if (!this.movedByForces && force.length() > 0) {
			p("Warning - setting additional force on non-moving entity");
		}
		this.setAdditionalForce(force.x, force.y, force.z);
	}


	public void setAdditionalForce(float x, float y, float z) {
		if (!this.movedByForces && (x != 0 || y != 0 || z != 0)) {
			p("Warning - setting additional force on non-moving entity");
		}
		this.additionalForce.set(x, y, z);
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


	/**
	 * If flase, the body is kinematic
	 * @return
	 */
	public boolean movedByForces() {
		return this.movedByForces;
	}


	/**
	 * This should only be called from SimplePhysicsController.
	 */
	public void removeFromParent_INTERNAL() {
		if (this.parent != null) {
			this.parent.remove(this);
			if (this.parent.getNumChildren() == 0) {
				this.physicsController.nodes.remove(this.parent.id);
			} else {
				this.parent.recalcBounds();
			}
		}
	}


	public static void p(String s) {
		System.out.println(s);
	}


	public void setNeverMoves(boolean b) {
		this.neverMoves = b;
	}


	public boolean getNeverMoves() {
		return this.neverMoves;
	}


	public void resetForces() {
		this.currentGravInc = 0;
		this.additionalForce.set(0, 0, 0);
		this.oneOffForce.set(0, 0, 0);
		this.isOnGround = false;
	}


	public float GetCurrentGravOffset() {
		return this.currentGravInc;
	}


	public void setCollidable(boolean b) {
		this.collidable = b;
	}


	public void setParent(SimpleNode<T> n) {
		if (this.parent != null) {
			throw new RuntimeException(this + " already has a parent: " + this.parent);
		}
		this.parent = n;
	}
}

