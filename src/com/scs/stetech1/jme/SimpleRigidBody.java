package com.scs.stetech1.jme;

import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.collision.UnsupportedCollisionException;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.scs.stetech1.components.ICollideable;
import com.scs.stetech1.entities.PhysicalEntity;

// todo - collision listener
public class SimpleRigidBody implements Collidable {
	
	// todo - bounciness, air friction
	private ICollisionChecker collChecker;
	//private PhysicalEntity entity;
	public Node node;
	private float gravY = -.01f; // todo - change if falling
	private Vector3f moveDir = new Vector3f();
	private Vector3f tmpMoveDir = new Vector3f();
	//private Vector3f tmpPrevPos = new Vector3f();
	private boolean isOnGround = false;
	
	private static final Vector3f DOWN = new Vector3f(0, -1, 0);

	public SimpleRigidBody(PhysicalEntity e, ICollisionChecker _collChecker) {
		super();

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


	/**
	 * Returns whether the move was completed
	 */
	private boolean move(Vector3f offset) {
		//tmpPrevPos.set(this.entity.getWorldTranslation());
		this.entity.adjustWorldTranslation(offset);
		boolean result = collChecker.checkForCollisions((ICollideable)this.entity);
		if (!result) {
			this.entity.adjustWorldTranslation(offset.multLocal(-1));
		}
		return result;
	}


	public void jump() {
		 if (isOnGround) {
			 this.gravY = 1f;
		 }
	}


	@Override
	public int collideWith(Collidable other, CollisionResults results) throws UnsupportedCollisionException {
		// TODO Auto-generated method stub
		return 0;
	}


    /*protected void checkOnGround() {
        //TempVars vars = TempVars.get();
        //Vector3f location = vars.vect1;
        //Vector3f rayVector = vars.vect2;
        //float height = getFinalHeight();
        /*location.set(localUp).multLocal(height).addLocal(this.location);
        rayVector.set(localUp).multLocal(-height - 0.1f).addLocal(location);
        List<PhysicsRayTestResult> results = space.rayTest(location, rayVector);
        vars.release();
        for (PhysicsRayTestResult physicsRayTestResult : results) {
            if (!physicsRayTestResult.getCollisionObject().equals(rigidBody)) {
                onGround = true;
                return;
            }
        }
        onGround = false;*/
    	/*Ray r = new Ray(this.entity.getWorldTranslation(), DOWN);
		boolean result = collChecker.checkForCollisions(r);
		return result;
    }*/

}
