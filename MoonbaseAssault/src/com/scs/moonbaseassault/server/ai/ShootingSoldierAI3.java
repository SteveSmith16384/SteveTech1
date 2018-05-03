package com.scs.moonbaseassault.server.ai;

import com.jme3.math.Vector3f;
import com.scs.moonbaseassault.entities.Computer;
import com.scs.moonbaseassault.entities.Floor;
import com.scs.moonbaseassault.entities.MapBorder;
import com.scs.moonbaseassault.entities.MoonbaseWall;
import com.scs.moonbaseassault.entities.SlidingDoor;
import com.scs.stevetech1.entities.AbstractAISoldier;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.server.IArtificialIntelligence;

import ssmith.lang.NumberFunctions;
import ssmith.util.RealtimeInterval;

public class ShootingSoldierAI3 implements IArtificialIntelligence {

	private static final float WAIT_FOR_DOOR_DURATION = 3;
	private static final boolean SHOOT_AT_ENEMY = true;

	private AbstractAISoldier soldierEntity;
	private Vector3f currDir;
	private RealtimeInterval checkForEnemyInt = new RealtimeInterval(1000);
	private PhysicalEntity currentTarget;
	private int animCode = 0;

	private float waitForSecs = 0; // e.g. wait for door to open

	public ShootingSoldierAI3(AbstractAISoldier _pe) {
		soldierEntity = _pe;

		currDir = new Vector3f();
		changeDirection(getRandomDirection()); // Start us pointing in the right direction
	}


	@Override
	public void process(AbstractGameServer server, float tpf_secs) {
		if (this.waitForSecs > 0) {
			this.waitForSecs -= tpf_secs;
		} 

		if (currentTarget != null) { // Find enemy
			boolean cansee = soldierEntity.canSee(this.currentTarget, 100f);
			if (!cansee) {
				soldierEntity.canSee(this.currentTarget, 100f); // todo - remove
				this.currentTarget = null;
				if (Globals.DEBUG_AI_SEE_PLAYER) {
					Globals.p("AI no longer see player");
				}
			}
		}
		if (currentTarget == null) { // Check we can still see enemy
			if (this.checkForEnemyInt.hitInterval()) {
				currentTarget = server.getTarget(this.soldierEntity, this.soldierEntity.side);
				if (Globals.DEBUG_AI_SEE_PLAYER && currentTarget != null) {
					Globals.p("AI can now see " + currentTarget);
				}
			}
		} else { // Face enemy
			Vector3f dir = this.currentTarget.getWorldTranslation().subtract(this.soldierEntity.getWorldTranslation()); // todo - don't create each time
			//this.currDir.subtractLocal();
			dir.y = 0;
			dir.normalizeLocal();
			this.changeDirection(dir);
		}

		if (currentTarget != null && SHOOT_AT_ENEMY) {
			soldierEntity.simpleRigidBody.getAdditionalForce().set(0, 0, 0); // Stop walking
			animCode = AbstractAvatar.ANIM_IDLE;
			this.soldierEntity.shoot(currentTarget);
		} else if (waitForSecs <= 0) {
			soldierEntity.simpleRigidBody.setAdditionalForce(this.currDir.mult(AbstractAISoldier.SPEED)); // Walk forwards
			animCode = AbstractAvatar.ANIM_WALKING;
		} else {
			soldierEntity.simpleRigidBody.getAdditionalForce().set(0, 0, 0); // Stop walking
			animCode = AbstractAvatar.ANIM_IDLE;
		}

	}


	@Override
	public void collided(PhysicalEntity pe) {
		if (pe instanceof Floor == false) {
			// Change direction to away from blockage, unless it's a doior
			if (pe instanceof Floor == false) {
				// Change direction to away from blockage, unless it's a doior
				if (pe instanceof MoonbaseWall || pe instanceof Computer || pe instanceof MapBorder) {
					//Globals.p("AISoldier has collided with " + pe);
					//changeDirection(currDir.mult(-1));
					changeDirection(getRandomDirection()); // Start us pointing in the right direction
				} else if (pe instanceof SlidingDoor) {
					this.waitForSecs += WAIT_FOR_DOOR_DURATION;
				}
			}
		}

	}


	private void changeDirection(Vector3f dir) {
		//Globals.p("Changing direction to " + dir);
		this.currDir.set(dir);
		soldierEntity.getMainNode().lookAt(soldierEntity.getWorldTranslation().add(currDir), Vector3f.UNIT_Y); // Point us in the right direction
	}


	private static Vector3f getRandomDirection() {
		int i = NumberFunctions.rnd(0,  3);
		switch (i) {
		case 0: return new Vector3f(1f, 0, 0);
		case 1: return new Vector3f(-1f, 0, 0);
		case 2: return new Vector3f(0f, 0, 1f);
		case 3: return new Vector3f(0f, 0, -1f);
		}
		throw new RuntimeException("Invalid direction: " + i);
	}


	@Override
	public Vector3f getDirection() {
		return currDir;
	}


	@Override
	public int getAnimCode() {
		return animCode;
	}


	@Override
	public PhysicalEntity getCurrentTarget() {
		return this.currentTarget;
	}

}
