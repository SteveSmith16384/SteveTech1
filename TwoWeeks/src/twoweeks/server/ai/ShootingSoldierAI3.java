package twoweeks.server.ai;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.Globals;

import ssmith.lang.NumberFunctions;
import ssmith.util.RealtimeInterval;
import twoweeks.entities.AISoldier;
import twoweeks.entities.Floor;
import twoweeks.entities.Terrain1;

public class ShootingSoldierAI3 implements IArtificialIntelligence {

	private static final boolean SHOOT_AT_ENEMY = true;

	private AISoldier soldierEntity;
	private Vector3f currDir;
	private RealtimeInterval checkForEnemyInt = new RealtimeInterval(1000);
	private PhysicalEntity currentTarget;
	private int animCode = 0;

	private float waitForSecs = 0; // e.g. wait for door to open

	public ShootingSoldierAI3(AISoldier _pe) {
		soldierEntity = _pe;

		currDir = new Vector3f();
		changeDirection(getRandomDirection()); // Start us pointing in the right direction
	}


	@Override
	public void process(AbstractEntityServer server, float tpf_secs) {
		if (this.waitForSecs > 0) {
			this.waitForSecs -= tpf_secs;
		} 

		if (currentTarget != null) {
			boolean cansee = soldierEntity.canSee(this.currentTarget, 100f);
			if (!cansee) {
				this.currentTarget = null;
			}
		}
		if (currentTarget == null) {
			if (this.checkForEnemyInt.hitInterval()) {
				currentTarget = server.getTarget(this.soldierEntity, this.soldierEntity.side);
			}
		} else {
			// Look at our target
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
			soldierEntity.simpleRigidBody.setAdditionalForce(this.currDir.mult(AISoldier.SPEED)); // Walk forwards
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
			if (pe instanceof Terrain1 == false) {
				Globals.p("AISoldier has collided with " + pe);
				//changeDirection(currDir.mult(-1));
				changeDirection(getRandomDirection()); // Start us pointing in the right direction
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
