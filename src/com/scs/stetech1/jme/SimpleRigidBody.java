package com.scs.stetech1.jme;

import com.jme3.math.Vector3f;
import com.scs.stetech1.components.ICollideable;
import com.scs.stetech1.entities.PhysicalEntity;

public class SimpleRigidBody {

	private ICollisionChecker collChecker;
	private PhysicalEntity entity;
	private float gravY = 0;
	private Vector3f moveDir = new Vector3f();
	private Vector3f tmpMoveDir = new Vector3f();

	public SimpleRigidBody(PhysicalEntity e, ICollisionChecker _collChecker) {
		super();

		//spatial = s;
		entity = e;
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
		if (moveDir.y != 0 || gravY != 0) {
			this.tmpMoveDir.set(0, moveDir.y+gravY, 0);
			if (!this.move(tmpMoveDir)) {
				moveDir.y = 0;
				gravY = 0; // reset gravY if not falling
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


	/**
	 * Returns whether the move was completed
	 */
	private boolean move(Vector3f offset) {
		this.entity.adjustWorldTranslation(offset);
		collChecker.checkForCollisions((ICollideable)this.entity);
		return true;
	}


}
