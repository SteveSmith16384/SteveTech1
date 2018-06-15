package com.scs.moonbaseassault.server.ai;

import java.awt.Point;

import com.jme3.math.Vector3f;
import com.scs.moonbaseassault.entities.AILaserBullet;
import com.scs.moonbaseassault.entities.AbstractAISoldier;
import com.scs.moonbaseassault.entities.Computer;
import com.scs.moonbaseassault.entities.Floor;
import com.scs.moonbaseassault.entities.MapBorder;
import com.scs.moonbaseassault.entities.MoonbaseWall;
import com.scs.moonbaseassault.entities.SlidingDoor;
import com.scs.moonbaseassault.server.MoonbaseAssaultServer;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.ITargetable;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.server.IArtificialIntelligence;

import ssmith.astar.WayPoints;
import ssmith.lang.NumberFunctions;
import ssmith.util.RealtimeInterval;

public class ShootingSoldierAI3 implements IArtificialIntelligence {

	private static final float VIEW_ANGLE_RADS = -1f; // See everywhere
	private static final float WAIT_FOR_DOOR_DURATION = 1.6f;
	private static final boolean SHOOT_AT_ENEMY = true;

	private AbstractAISoldier soldierEntity;
	private Vector3f currDir;
	private RealtimeInterval checkForEnemyInt;
	private ITargetable currentTarget;
	private int animCode = 0;
	private float waitForSecs = 0; // e.g. wait for door to open
	private float maintainDirectionForSecs = 0; // e.g. collided with comrade

	private boolean attacker; // Might need this in the future
	private FindComputerThread fcThread;
	private WayPoints route = null;
	private float speed;
	private boolean walks;

	public ShootingSoldierAI3(AbstractAISoldier _pe, boolean _attacker, boolean _walks) {
		super();

		soldierEntity = _pe;
		attacker = _attacker;
		checkForEnemyInt = new RealtimeInterval(NumberFunctions.rnd(1200,  2000)); // Avoid AI all shooting at the same time
		currDir = new Vector3f();
		walks = _walks;
		if (walks) {
			speed = AbstractAISoldier.WALKING_SPEED;
		} else {
			speed = AbstractAISoldier.RUNNING_SPEED;
		}
		changeDirection(getRandomDirection()); // Start us pointing in the right direction
	}


	@Override
	public void process(AbstractGameServer server, float tpf_secs) {
		if (this.waitForSecs > 0) {
			this.waitForSecs -= tpf_secs;
		} 
		if (this.maintainDirectionForSecs > 0) {
			this.maintainDirectionForSecs -= tpf_secs;
		}

		if (currentTarget != null) { // Check we can still see enemy
			if (!this.currentTarget.isAlive()) {
				this.currentTarget = null;
			} else {
				boolean cansee = soldierEntity.canSee((PhysicalEntity)this.currentTarget, AILaserBullet.RANGE, VIEW_ANGLE_RADS);
				if (!cansee) {
					this.currentTarget = null;
					maintainDirectionForSecs = 4f; // Walk towards them for 4 secs

					if (Globals.DEBUG_AI_TARGETTING) {
						Globals.p("AI no longer see target");
					}
				}
			}
		}


		if (this.checkForEnemyInt.hitInterval()) {
			currentTarget = server.getTarget(this.soldierEntity, this.soldierEntity.side, AILaserBullet.RANGE, VIEW_ANGLE_RADS);
			if (Globals.DEBUG_AI_TARGETTING && currentTarget != null) {
				Globals.p("AI can now see " + currentTarget);
			}
		}

		if (currentTarget != null) {
			PhysicalEntity pe = (PhysicalEntity)this.currentTarget;
			Vector3f dir = pe.getWorldTranslation().subtract(this.soldierEntity.getWorldTranslation()); // todo - don't create each time
			dir.y = 0;
			dir.normalizeLocal();
			this.changeDirection(dir);

			soldierEntity.simpleRigidBody.getAdditionalForce().set(0, 0, 0); // Stop walking
			animCode = AbstractAvatar.ANIM_IDLE;
			if (SHOOT_AT_ENEMY) {
				this.soldierEntity.shoot((PhysicalEntity)currentTarget);
			}
		}

		if (currentTarget == null) { // No current enemy
			if (this.route == null) { // this.attacker &&
				//if (this.attacker || (this.computer == null))
				soldierEntity.simpleRigidBody.getAdditionalForce().set(0, 0, 0); // Stop walking
				animCode = AbstractAvatar.ANIM_IDLE;
				getRoute(server);

			} else if (waitForSecs > 0) { // Wait for door
				soldierEntity.simpleRigidBody.getAdditionalForce().set(0, 0, 0); // Stop walking
				animCode = AbstractAvatar.ANIM_IDLE;

			} else if (maintainDirectionForSecs > 0) {
				soldierEntity.simpleRigidBody.setAdditionalForce(this.currDir.mult(speed)); // Walk forwards
				animCode = walks? AbstractAvatar.ANIM_WALKING : AbstractAvatar.ANIM_RUNNING;

			} else {
				if (route != null) { //this.attacker && 
					checkRoute();
				}
				soldierEntity.simpleRigidBody.setAdditionalForce(this.currDir.mult(speed)); // Walk forwards
				animCode = walks? AbstractAvatar.ANIM_WALKING : AbstractAvatar.ANIM_RUNNING;
			}
		}
	}


	/*
	 * Called if the unit has no route
	 */
	private void getRoute(AbstractGameServer server) {
		if (fcThread == null) {
			this.fcThread = new FindComputerThread((MoonbaseAssaultServer)server, soldierEntity);
			this.fcThread.start();
		} else if (!fcThread.isAlive()) {
			this.route = this.fcThread.route;
			fcThread = null;
		} else {
			// Still waiting
		}
	}


	private void checkRoute() {
		if (this.route.isEmpty() || this.route.size() <= 1) { // Stop at last one since the dest is a comp0uter, and we don't want to walk into it
			this.route = null;
		} else {
			Point p = this.route.get(0);
			Vector3f dest = new Vector3f(p.x+0.5f, this.soldierEntity.getWorldTranslation().y, p.y+0.5f); // todo - don't create each time
			float dist = this.soldierEntity.getWorldTranslation().distance(dest);
			if (dist < .3f) {
				this.route.remove(0);
			} else {
				Vector3f dir = dest.subtract(this.soldierEntity.getWorldTranslation()).normalizeLocal();
				changeDirection(dir);
			}
		}
	}


	@Override
	public void collided(PhysicalEntity pe) {
		if (pe instanceof Floor == false) {
			// Change direction to away from blockage, unless it's a door
			if (pe instanceof MoonbaseWall || pe instanceof Computer || pe instanceof MapBorder) {
				//Globals.p("AISoldier has collided with " + pe);
				changeDirection(getRandomDirection());
				maintainDirectionForSecs = 1f; // Hopefully allow us to walk around the corner which we've probably clipped
				this.route = null;
			} else if (pe instanceof SlidingDoor) {
				this.waitForSecs += WAIT_FOR_DOOR_DURATION;
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
	public ITargetable getCurrentTarget() {
		return this.currentTarget;
	}


	@Override
	public void wounded(ICausesHarmOnContact collider) {
		if (collider.getActualShooter() != null) {
			PhysicalEntity pe = (PhysicalEntity)collider.getActualShooter();
			Vector3f dir = pe.getWorldTranslation().subtract(soldierEntity.getWorldTranslation()).normalizeLocal();
			this.changeDirection(dir);
		}
	}


}
