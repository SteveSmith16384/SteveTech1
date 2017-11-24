package com.scs.simplephysics;

import java.util.Collection;

import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.collision.UnsupportedCollisionException;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class SimpleRigidBody implements Collidable {
	
	private static final float AIR_FRICTION = 0.99f;
	//private static final Vector3f DOWN = new Vector3f(0, -1, 0);

	private ISimplePhysicsController physicsController;
	//private Node node;
	private ISimplePhysicsEntity simplePhysicsEntity;
	private float gravY = -.02f; // todo - change if falling
	private Vector3f moveDir = new Vector3f();
	private Vector3f tmpMoveDir = new Vector3f();
	private boolean isOnGround = false;
	private float bounciness = .5f;

	private CollisionResults collisionResults = new CollisionResults();

	public SimpleRigidBody(ISimplePhysicsEntity _entity, ISimplePhysicsController _collChecker) {
		super();

		simplePhysicsEntity = _entity;
		//node = simplePhysicsEntity.getNode();
		physicsController = _collChecker;
	}

	
	public ISimplePhysicsEntity getSimplePhysicsEntity() {
		return this.simplePhysicsEntity;
	}
	

	public void process(float tpf_secs) {
		// Move X
		if (moveDir.x != 0) {
			moveDir.x = moveDir.x * AIR_FRICTION;
			this.tmpMoveDir.set(moveDir.x, 0, 0);
			ISimplePhysicsEntity collidedWith = this.move(tmpMoveDir);
			if (collidedWith != null) {
				//SimpleRigidBody body = collidedWith.getSimpleRigidBody();
				float bounce = this.bounciness;// * body.bounciness;
				moveDir.x = moveDir.x * bounce;
			}
		}
		// Move Y
		isOnGround = false;
		if (moveDir.y != 0 || gravY != 0) {
			moveDir.y = moveDir.y * AIR_FRICTION;
			float totalOffset = moveDir.y+gravY;
			this.tmpMoveDir.set(0, totalOffset, 0);
			ISimplePhysicsEntity collidedWith = this.move(tmpMoveDir);
			if (collidedWith != null) {
				//SimpleRigidBody body = collidedWith.getSimpleRigidBody();
				float bounce = this.bounciness;// * body.bounciness;
				moveDir.y = moveDir.y * bounce;
				gravY = 0; // reset gravY if not falling
				if (totalOffset < 0) {
					isOnGround = true;
				}
			}

		}

		//Move z
		if (moveDir.z != 0) {
			moveDir.z = moveDir.z * AIR_FRICTION;
			this.tmpMoveDir.set(0, 0, moveDir.z);
			ISimplePhysicsEntity collidedWith = this.move(tmpMoveDir);
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
	private ISimplePhysicsEntity move(Vector3f offset) {
		this.simplePhysicsEntity.getNode().move(offset);
		ISimplePhysicsEntity wasCollision = checkForCollisions();
		if (wasCollision != null) {
			this.simplePhysicsEntity.getNode().move(offset.negateLocal()); // Move back
		}
		return wasCollision;
	}


	/*
	 * Returns object they collided with
	 */
	public ISimplePhysicsEntity checkForCollisions() {
		collisionResults.clear();
		ISimplePhysicsEntity collidedWith = null;
		Collection<Object> entities = physicsController.getEntities();
		synchronized (entities) {
			// Loop through the entities
			for(Object e : entities) {
				if (e instanceof ISimplePhysicsEntity) {
					if (e != simplePhysicsEntity) { // Don't check ourselves
						ISimplePhysicsEntity ic = (ISimplePhysicsEntity)e;
						if (this.collideWith(ic.getNode().getWorldBound(), collisionResults) > 0) {
							collidedWith = ic;
							this.physicsController.collisionOccurred(this, e);
						}
					}
				}
			}
		}
		return collidedWith;
	}


	@Override
	public int collideWith(Collidable other, CollisionResults results) throws UnsupportedCollisionException {
		return this.simplePhysicsEntity.getNode().collideWith(other, results);
	}


	public void jump() {
		if (isOnGround) {
			this.gravY = 1f;
		}
	}


}
