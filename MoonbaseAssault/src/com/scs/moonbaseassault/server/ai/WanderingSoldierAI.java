package com.scs.moonbaseassault.server.ai;

import com.jme3.math.Vector3f;
import com.scs.moonbaseassault.entities.Computer;
import com.scs.moonbaseassault.entities.Floor;
import com.scs.moonbaseassault.entities.MapBorder;
import com.scs.moonbaseassault.entities.MoonbaseWall;
import com.scs.stevetech1.entities.AbstractAISoldier;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.IArtificialIntelligence;

import ssmith.lang.NumberFunctions;

public class WanderingSoldierAI implements IArtificialIntelligence {

	private PhysicalEntity physicalEntity;
	private Vector3f currDir;

	public WanderingSoldierAI(PhysicalEntity _pe) {
		physicalEntity = _pe;

		currDir = this.getRandomDirection();
		changeDirection(currDir); // Start us pointing in the right direction
	}


	@Override
	public void process(AbstractEntityServer server, float tpf_secs) {
		if (this.currDir.length() == 0) {
			this.currDir = this.getRandomDirection();
		}
		//Globals.p("Currdir: " + this.currDir);
		physicalEntity.simpleRigidBody.setAdditionalForce(this.currDir.mult(AbstractAISoldier.SPEED)); // Walk forwards

	}


	@Override
	public void collided(PhysicalEntity pe) {
		if (pe instanceof Floor == false) {
			// Change direction to away from blockage, unless it's a doior
			if (pe instanceof MoonbaseWall || pe instanceof Computer || pe instanceof MapBorder) {
				//Globals.p("AISoldier has collided with " + pe);
				changeDirection(currDir.mult(-1));
			}
		}

	}


	private void changeDirection(Vector3f dir) {
		//Globals.p("Changing direction to " + dir);
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


	@Override
	public Vector3f getDirection() {
		return currDir;
	}


	@Override
	public int getAnimCode() {
		return AbstractAvatar.ANIM_WALKING;
	}


	@Override
	public PhysicalEntity getCurrentTarget() {
		return null;
	}

}
