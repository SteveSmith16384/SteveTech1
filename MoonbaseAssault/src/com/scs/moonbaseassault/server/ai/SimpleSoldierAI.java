package com.scs.moonbaseassault.server.ai;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.scs.moonbaseassault.entities.AISoldier;
import com.scs.moonbaseassault.entities.Computer;
import com.scs.moonbaseassault.entities.Floor;
import com.scs.moonbaseassault.entities.InvisibleMapBorder;
import com.scs.moonbaseassault.entities.MoonbaseWall;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.Globals;

import ssmith.lang.NumberFunctions;

public class SimpleSoldierAI implements IArtificialIntelligence {

	private static final float WAIT_FOR_DOOR_DURATION = 3;

	private PhysicalEntity physicalEntity;
	private Vector3f currDir;

	public SimpleSoldierAI(PhysicalEntity _pe) {
		physicalEntity = _pe;

		currDir = this.getRandomDirection();
		changeDirection(currDir); // Start us pointing in the right direction
	}


	@Override
	public void process(float tpf_secs) {
		if (this.currDir.length() == 0) {
			this.currDir = this.getRandomDirection();
		}
		if (!Globals.DEBUG_CAN_SEE) {
			//Globals.p("Currdir: " + this.currDir);
			physicalEntity.simpleRigidBody.setAdditionalForce(this.currDir.mult(AISoldier.SPEED)); // Walk forwards
		} else {
			/*if (MoonbaseAssaultServer.player != null) {
				boolean cansee = this.canSee(MoonbaseAssaultServer.player, 100f);
				if (cansee) {
					//Globals.p("Soldier can see player");
				}
			}*/
		}


	}


	@Override
	public void collided(PhysicalEntity pe) {
		if (pe instanceof Floor == false) {
			// Change direction to away from blockage
			if (pe instanceof MoonbaseWall || pe instanceof Computer || pe instanceof InvisibleMapBorder) {
				Globals.p("AISoldier has collided with " + pe);
				changeDirection(currDir.mult(-1));
			}
		}

	}


	private void changeDirection(Vector3f dir) {
		Globals.p("Changing direction to " + dir);
		this.currDir.set(dir);
		physicalEntity.getMainNode().lookAt(physicalEntity.getWorldTranslation().add(currDir), Vector3f.UNIT_Y); // Point us in the right direction
	}


	private Vector3f getRandomDirection() {
		int i = NumberFunctions.rnd(0,  3);
		switch (i) {
		case 0: return new Vector3f(1f, 0, 0);
		case 1: return new Vector3f(-1f, 0, 0);
		case 2: return new Vector3f(0f, 0, 1f);
		case 3: return new Vector3f(0f, 0, -1f);
		}
		throw new RuntimeException("Invalid direction: " + i);
	}


}
