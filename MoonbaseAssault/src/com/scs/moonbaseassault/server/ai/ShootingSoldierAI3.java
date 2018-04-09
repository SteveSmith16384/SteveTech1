package com.scs.moonbaseassault.server.ai;

import com.jme3.math.Vector3f;
import com.scs.moonbaseassault.entities.AISoldier;
import com.scs.moonbaseassault.entities.Computer;
import com.scs.moonbaseassault.entities.Floor;
import com.scs.moonbaseassault.entities.MapBorder;
import com.scs.moonbaseassault.entities.MoonbaseWall;
import com.scs.moonbaseassault.entities.SlidingDoor;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.Globals;

import ssmith.lang.NumberFunctions;
import ssmith.util.RealtimeInterval;

public class ShootingSoldierAI3 implements IArtificialIntelligence {

	private static final float WAIT_FOR_DOOR_DURATION = 3;
	private static final boolean SHOOT_AT_ENEMY = true;

	private PhysicalEntity physicalEntity;
	private Vector3f currDir;
	private RealtimeInterval checkForEnemyInt = new RealtimeInterval(1000);
	private PhysicalEntity currentEnemy;

	private float waitForSecs = 0; // e.g. wait for door to open

	public ShootingSoldierAI3(PhysicalEntity _pe) {
		physicalEntity = _pe;

		changeDirection(getRandomDirection()); // Start us pointing in the right direction
	}


	@Override
	public void process(float tpf_secs) {
		if (this.waitForSecs > 0) {
			this.waitForSecs -= tpf_secs;
		} 

		if (currentEnemy != null) {
			boolean cansee = physicalEntity.canSee(this.currentEnemy, 100f);
			if (!cansee) {
				this.currentEnemy = null;
			}
		}
		if (this.checkForEnemyInt.hitInterval()) {
			// todo - find enemy
		}
		if (currentEnemy != null) {
			// todo - shoot!
		} else if (waitForSecs < 0) {
			physicalEntity.simpleRigidBody.setAdditionalForce(this.currDir.mult(AISoldier.SPEED)); // Walk forwards
		}

	}


	@Override
	public void collided(PhysicalEntity pe) {
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


	private void changeDirection(Vector3f dir) {
		//Globals.p("Changing direction to " + dir);
		this.currDir.set(dir);
		physicalEntity.getMainNode().lookAt(physicalEntity.getWorldTranslation().add(currDir), Vector3f.UNIT_Y); // Point us in the right direction
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
		if (this.waitForSecs > 0) {
			return AbstractAvatar.ANIM_IDLE;
		} else {
			return AbstractAvatar.ANIM_WALKING;
		}
	}

}
