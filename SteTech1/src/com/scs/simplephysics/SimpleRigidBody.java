package com.scs.simplephysics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.collision.UnsupportedCollisionException;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.scs.stevetech1.entities.VoxelTerrainEntity;
import com.scs.stevetech1.server.Globals;

public class SimpleRigidBody<T> implements Collidable, Savable { // Implementing Savable is a hack so we can use this class as UserData

	private static final boolean AUTOMOVE_BY_FORCE = false; // Doesn't work since move() moves entity back to original pos on collision
	private static final boolean DEBUG_AUTOMOVING = false;
	private static final boolean DEBUG_STEPS_SLOPES = false;

	private static final float AUTOMOVE_FRAC = .1f;
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
	public boolean canWalkUpSteps = false;
	public boolean removed = false;
	private boolean neverMoves = false; // More efficient if true - todo - reverse the logic!
	private boolean isSolid = true; // otherwise, other SRBs can pass through it

	private SimpleNode<T> parentNode;

	// Temp caches
	private Vector3f tmpPrevPos = new Vector3f();
	private Vector3f tmpPos = new Vector3f();

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


	/**
	 * if false, the entity is kinemtic, ie. only moved manually, not by forces.
	 * @param b
	 */
	public void setMovedByForces(boolean b) {
		this.movedByForces = b;
	}


	public void setAerodynamicness(float f) {
		this.aerodynamicness = f;
	}


	public void setGravity(float g) {
		this.gravInc = g;
	}


	public void process(float tpfSecs) {
		if (tpfSecs > 0.1f) {
			tpfSecs = 0.1f; // Prevent stepping too far
		} else if (tpfSecs < 0.0001f) {
			System.err.println("Warning: delta is too small; you will get rounding errors!");
		}

		if (this.movedByForces) {
			automoveForce.set(0, 0, 0);

			// Check we're not already colliding *before* we've even moved
			boolean doBounce = true;
			List<SimpleRigidBody<T>> crs = this.checkForCollisions(true);
			if (crs.size() != 0) {
				if (DEBUG_AUTOMOVING) {
					System.err.println("Warning: " + this + " has collided prior to move, with " + crs.toString());
				}
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
					this.tmpMoveDir.set(totalOffset * tpfSecs, 0, 0);
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
					this.tmpMoveDir.set(0, 0, totalOffset * tpfSecs);
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
					this.tmpMoveDir.set(0, totalOffset * tpfSecs, 0);
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
					}
				}
				if (!collided) {
					currentGravInc = currentGravInc + (gravInc * tpfSecs); // Fall faster
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

		SimpleRigidBody<T> cr = crs.get(0);

		BoundingBox ourBB = (BoundingBox)this.getBoundingBox();
		float ourHeight = ourBB.getCenter().y - ourBB.getYExtent();

		// Check for stairs first.  treat stairs and slopes differently since we handle the upward force differently
		if (cr.simpleEntity.getCollidable() instanceof TerrainQuad == false && cr.simpleEntity.getCollidable() instanceof BoundingBox) {
			if (DEBUG_STEPS_SLOPES) {
				p("Checking for stairs");
			}
			// Check simple BB against simple BB 
			BoundingBox theirBB = (BoundingBox)cr.getBoundingBox();
			float nextHeight = theirBB.getCenter().y + (theirBB.getYExtent());
			float heightDiff = nextHeight - ourHeight;

			if (heightDiff > 0 && heightDiff <= SimplePhysicsController.MAX_STEP_HEIGHT) {
				if (DEBUG_STEPS_SLOPES) {
					p("Going up step: height=" + heightDiff + ", stepForce=" + this.physicsController.getStepForce());
				}
				this.oneOffForce.y += Math.sqrt(heightDiff) * this.physicsController.getStepForce();
				return true;
			} else {
				if (DEBUG_STEPS_SLOPES) {
					p("NOT Going up step: height=" + heightDiff);
				}
			}

		} else {
			if (DEBUG_STEPS_SLOPES) {
				p("Checking for slope");
			}
			float DEF_EXTENT = 0.1f; // How far ahead to check the slope
			float extent = ourBB.getXExtent() + DEF_EXTENT; // todo - get proper edge of BB

			CollisionResults rayCRs = new CollisionResults();
			tmpPos.set(moveOffset).normalizeLocal().multLocal(extent);
			Vector3f newPos = tmpPos.addLocal(posBeforeMove);
			Ray nextRay = new Ray(newPos, DOWN_VEC);
			rayCRs.clear();
			cr.simpleEntity.getCollidable().collideWith(nextRay, rayCRs);
			if (rayCRs.size() > 0) { 
				float nextHeight = rayCRs.getClosestCollision().getContactPoint().y;
				float heightDiff = nextHeight - ourHeight;
				float ratio = heightDiff / DEF_EXTENT; // <1 = 45 degrees or walkable
				if (heightDiff > 0 && heightDiff <= SimplePhysicsController.MAX_STEP_HEIGHT && ratio <= 1f) { // todo - make ratio limit a config
					if (DEBUG_STEPS_SLOPES) {
						p("Walking up ramp! heightDiff=" + heightDiff + "; ratio= " + ratio);
					}
					this.oneOffForce.y += Math.sqrt(ratio) * this.physicsController.getRampForce(); // 3f; //  Adjust by steepness
					return true;
				} else {
					if (DEBUG_STEPS_SLOPES) {
						p("NOT Going up step: heightDiff=" + heightDiff);
					}
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
		List<SimpleRigidBody<T>> crs = checkForCollisions(true);
		if (crs.size() > 0) {
			this.simpleEntity.moveEntity(offset.negate()); // Move back
		}
		return crs;
	}


	public List<SimpleRigidBody<T>> checkForCollisions(boolean notify) {
		List<SimpleRigidBody<T>> crs = new ArrayList<SimpleRigidBody<T>>();

		CollisionResults tempCollisionResults = new CollisionResults(); // Avoid creating a new one each time

		for(SimpleNode<T> node : this.physicsController.nodes.values()) {
			node.getCollisions(this, crs, tempCollisionResults, notify);
		}
		// Check against moving/big entities
		List<SimpleRigidBody<T>> entities = physicsController.movingEntities;
		synchronized (entities) {
			// Loop through the entities
			for (int i=0 ; i<entities.size() ; i++) {
				SimpleRigidBody<T> e = entities.get(i);
				if (this.checkSRBvSRB(e, tempCollisionResults, notify)) {
					crs.add(e);
				}
			}
		}
		return crs;
	}


	/**
	 * @param e
	 * @return whether the two SRB's collided.
	 */
	public boolean checkSRBvSRB(SimpleRigidBody<T> e, CollisionResults tempCollisionResults, boolean notify) {
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
					res = tq.collideWith(ray, tempCollisionResults);
					if (res > 0) {
						// Compare positions
						Vector3f pos = tempCollisionResults.getClosestCollision().getContactPoint();
						if (bv.getCenter().y-bv.getYExtent() >= pos.y) {
							return false;
						} else {
							//p("Hit terrain");
						}
					}

				} else if (this.simpleEntity.getCollidable() instanceof VoxelTerrainEntity || e.simpleEntity.getCollidable() instanceof VoxelTerrainEntity) {
					VoxelTerrainEntity tq = null;
					BoundingBox bv = null;
					if (this.simpleEntity.getCollidable() instanceof VoxelTerrainEntity) {
						tq = (VoxelTerrainEntity)this.simpleEntity.getCollidable();
						bv = e.getBoundingBox();
					} else {
						tq = (VoxelTerrainEntity)e.simpleEntity.getCollidable();
						bv = this.getBoundingBox();
					}
					/*
					Vector3f start = bv.getCenter().clone();
					start.y = 255f;
					Ray ray = new Ray(start, DOWN_VEC);
					res = tq.collideWith(ray, tempCollisionResults);
					if (res > 0) {
						// Compare positions
						Vector3f pos = tempCollisionResults.getClosestCollision().getContactPoint();
						if (bv.getCenter().y-bv.getYExtent() >= pos.y) {
							return false;
						} else {
							//p("Hit terrain");
						}
					}
					*/

				} else if (this.simpleEntity.getCollidable() instanceof BoundingVolume == false && e.simpleEntity.getCollidable() instanceof BoundingVolume == false) {
					res = this.meshVMesh(e, tempCollisionResults) ? 1 : 0;

				} else {
					res = this.collideWith(e.simpleEntity.getCollidable(), tempCollisionResults);

				}
				if (res > 0) {
					if (notify) {
						this.physicsController.getCollisionListener().collisionOccurred(this, e);
					}
					if (this.isSolid && e.isSolid) {
						return true;
					}
				}
			}
		}
		return false;
	}


	private boolean meshVMesh(SimpleRigidBody<T> e, CollisionResults tempCollisionResults) {
		Node s1 = (Node)e.simpleEntity.getCollidable();
		BoundingVolume bv1 = (BoundingVolume)s1.getWorldBound();
		int res1 = this.collideWith(bv1, tempCollisionResults);

		// They are the most complex
		Node s2 = (Node)this.simpleEntity.getCollidable();
		BoundingVolume bv2 = (BoundingVolume)s2.getWorldBound();
		int res2 = bv2.collideWith(e.simpleEntity.getCollidable(), tempCollisionResults);

		return res1 > 0 && res2 > 0; // Only collide if both collisions fire

	}


	/**
	 * If you get UnsupportedCollisionException, it means you're trying to collide a mesh with a mesh.
	 */
	@Override
	public int collideWith(Collidable other, CollisionResults results) throws UnsupportedCollisionException {
		return this.simpleEntity.getCollidable().collideWith(other, results);
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
		//this.modelComplexity = i; No longer used
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
		if (this.parentNode != null) {
			this.parentNode.remove(this);
			if (this.parentNode.getNumChildren() == 0) {
				this.physicsController.nodes.remove(this.parentNode.id);
			} else {
				this.parentNode.recalcBounds();
			}
		}
	}


	public static void p(String s) {
		System.out.println(s);
	}


	/**
	 * Makes collisions more efficient.
	 * @param b
	 */
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


	public float getCurrentGravOffset() {
		return this.currentGravInc;
	}


	/**
	 * Non-solid SRBs will pass through other SRB's, but will still have collisions fired 
	 * @param s
	 */
	public void setSolid(boolean s) {
		this.isSolid = s;
	}


	public void setParentNode(SimpleNode<T> n) {
		if (this.parentNode != null) {
			throw new RuntimeException(this + " already has a parent: " + this.parentNode);
		}
		this.parentNode = n;
	}


	@Override
	public void write(JmeExporter ex) throws IOException {
		// Do nothing
		
	}


	@Override
	public void read(JmeImporter im) throws IOException {
		// Do nothing
		
	}
}

