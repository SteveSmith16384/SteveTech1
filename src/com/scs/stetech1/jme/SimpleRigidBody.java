package com.scs.stetech1.jme;

import java.util.Iterator;

import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.collision.UnsupportedCollisionException;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

// todo - collision listener
public class SimpleRigidBody implements Collidable {

	// todo - bounciness, air friction
	private ISimplePhysicsController collChecker;
	public Node node;
	public ISimplePhysicsEntity simplePhysicsEntity;
	private float gravY = -.01f; // todo - change if falling
	private Vector3f moveDir = new Vector3f();
	private Vector3f tmpMoveDir = new Vector3f();
	private boolean isOnGround = false;

	private static final Vector3f DOWN = new Vector3f(0, -1, 0);

	public SimpleRigidBody(ISimplePhysicsEntity _entity, ISimplePhysicsController _collChecker) {
		super();

		simplePhysicsEntity = _entity;
		node = simplePhysicsEntity.getNode();
		//node.setModelBound(new BoundingBox());
		//node.updateModelBound();
		collChecker = _collChecker;
	}


	public void process(float tpf_secs) {
		// Move X
		if (moveDir.x != 0) {
			this.tmpMoveDir.set(moveDir.x, 0, 0);
			if (!this.move(tmpMoveDir)) {
				moveDir.x = 0;
			}
		}
		// Move Y
		isOnGround = false;
		if (moveDir.y != 0 || gravY != 0) {
			float totalOffset = moveDir.y+gravY;
			this.tmpMoveDir.set(0, totalOffset, 0);
			if (!this.move(tmpMoveDir)) {
				moveDir.y = 0;
				gravY = 0; // reset gravY if not falling
				if (totalOffset < 0) {
					isOnGround = true;
				}
			}
		}

		//Move z
		if (moveDir.z != 0) {
			this.tmpMoveDir.set(0, 0, moveDir.z);
			if (!this.move(tmpMoveDir)) {
				moveDir.z = 0;
			}
		}

	}


	/*
	 * Returns whether the move was completed
	 */
	private boolean move(Vector3f offset) {
		this.simplePhysicsEntity.getSimpleRigidBody().node.getLocalTranslation().addLocal(offset);
		this.simplePhysicsEntity.getSimpleRigidBody().node.updateGeometricState(); // todo - need this?
		boolean wasCollision = checkForCollisions();
		if (wasCollision) {
			this.simplePhysicsEntity.getNode().getLocalTranslation().subtractLocal(offset); // Move back
			this.simplePhysicsEntity.getSimpleRigidBody().node.updateGeometricState(); // todo - need this?
			return false;
		}
		return true;
	}


	/*
	 * Returns true if there was a collision
	 */
	public boolean checkForCollisions() {
		boolean wasCollision = false; // this
		CollisionResults res = new CollisionResults();
		Iterator<Object> it = collChecker.getEntities();
		//synchronized (entities) {
		// Loop through the entities
		while (it.hasNext()) { // todo - sync
			Object e = it.next();
			if (e instanceof ISimplePhysicsEntity) {
				if (e != simplePhysicsEntity) { // Don't check ourselves
					ISimplePhysicsEntity ic = (ISimplePhysicsEntity)e;
					if (this.collideWith(ic.getNode().getWorldBound(), res) > 0) {
						this.collChecker.collisionOccurred(this, e);
						wasCollision = true;
					}
				}
			}
		}
		return wasCollision;
	}


	@Override
	public int collideWith(Collidable other, CollisionResults results) throws UnsupportedCollisionException {
		//SimpleRigidBody o =(SimpleRigidBody)other;
		BoundingVolume bv = (BoundingVolume)other;
		node.updateGeometricState(); // todo - remove?
		node.updateModelBound(); //node.getLocalTranslation();  // todo - remove?
		return this.node.collideWith(bv, results);
	}


	public void jump() {
		if (isOnGround) {
			this.gravY = 1f;
		}
	}


}
