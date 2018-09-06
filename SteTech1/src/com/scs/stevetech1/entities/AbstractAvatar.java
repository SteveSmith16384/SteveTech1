package com.scs.stevetech1.entities;

import com.jme3.collision.Collidable;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.scs.simplephysics.SimpleCharacterControl;
import com.scs.stevetech1.avatartypes.IAvatarControl;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IAffectedByPhysics;
import com.scs.stevetech1.components.IAvatarModel;
import com.scs.stevetech1.components.ICalcHitInPast;
import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IDontCollideWithComrades;
import com.scs.stevetech1.components.IPlayerControlled;
import com.scs.stevetech1.components.IProcessByServer;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;

public abstract class AbstractAvatar extends PhysicalEntity implements IPlayerControlled, IProcessByServer, ICanShoot, IAffectedByPhysics, IDontCollideWithComrades {

	// Animation Codes
	public static final int ANIM_IDLE = 0;
	public static final int ANIM_WALKING = 1;
	public static final int ANIM_RUNNING = 2;
	public static final int ANIM_JUMP = 3;
	public static final int ANIM_SHOOTING = 4;
	public static final int ANIM_DIED = 5;
	public static final int ANIM_ATTACK = 6;

	protected IInputDevice input;

	public final int playerID;
	protected Geometry bbGeom; // Non-rotating box for collisions
	public IAbility[] ability = new IAbility[2];
	public byte side = -1;
	protected IAvatarModel avatarModel;

	protected boolean alive = true;
	protected float restartTimeSecs, invulnerableTimeSecs;

	private float health;
	public int currentAnimCode = -1;

	protected IAvatarControl avatarControl;

	public AbstractAvatar(IEntityController _game, int avatarType, int _playerID, IInputDevice _input, int eid, byte _side, IAvatarModel _avatarModel, IAvatarControl _avatarControl) {
		super(_game, eid, avatarType, "Player", true, false, true);

		playerID = _playerID;
		input = _input;
		side =_side;
		avatarModel = _avatarModel;
		avatarControl = _avatarControl;

		Box box = new Box(avatarModel.getSize().x/2, avatarModel.getSize().y/2, avatarModel.getSize().z/2);
		bbGeom = new Geometry("bbGeom_" + entityName, box);
		bbGeom.setLocalTranslation(0, avatarModel.getSize().y/2, 0); // origin is centre!
		bbGeom.setCullHint(CullHint.Always); // Don't draw ourselves

		this.getMainNode().attachChild(bbGeom);
		
		avatarControl.init(this);

		this.simpleRigidBody = this.avatarControl.getSimpleRigidBody();
	}


	protected void serverAndClientProcess(AbstractGameServer server, IClientApp client, float tpf_secs, long serverTime) {
		if (Globals.STRICT) {
			if (ability[0] == null) {
				Globals.p("Warning - no ability0!");
			}
		}

		this.avatarControl.process();

		if (Globals.SHOW_AVATAR_POS) {
			Globals.p("pos=" + this.bbGeom.getWorldTranslation() + "  time=" + serverTime);
		}

		simpleRigidBody.process(tpf_secs);

		if (!this.isAlive()) {
			currentAnimCode = ANIM_DIED;
		} else {
			this.currentAnimCode = this.avatarControl.getCurrentAnimCode();
		}
	}


	@Override
	public boolean sendUpdates() {
		return true; // Always send for avatars
	}


	public byte getSide() {
		return side;
	}


	@Override
	public void resetPlayerInput() {
		SimpleCharacterControl<PhysicalEntity> simplePlayerControl = (SimpleCharacterControl<PhysicalEntity>)this.simpleRigidBody;
		simplePlayerControl.getAdditionalForce().set(0, 0, 0);
	}


	@Override
	public Vector3f getBulletStartPos() {
		Vector3f pos = this.getWorldTranslation().add(0, avatarModel.getBulletStartHeight(), 0);
		return pos;
	}


	@Override
	public void markForRemoval() {
		for (int i=0 ; i< this.ability.length ; i++) {
			if (this.ability[i] != null) {
				this.game.markForRemoval(this.ability[i].getID());
			}
		}
		super.markForRemoval();
	}


	public ICalcHitInPast getAnyAbilitiesShootingInPast() {
		for (int i=0 ; i< this.ability.length ; i++) {
			if (this.ability[i] != null) {
				if (this.ability[i] instanceof ICalcHitInPast) {
					if (this.ability[i].isGoingToBeActivated()) {
						return (ICalcHitInPast)this.ability[i];
					}
				}
			}
		}
		return null;
	}


	public void setAlive(boolean a) {
		if (this.alive != a) {
			this.alive = a;
			if (Globals.DEBUG_SET_ALIVE) {
				Globals.p("Avatar now alive=" + this.alive);
			}
			// Note that the client has been told they have died, but the player shouldn't know until the client render time has caught up!
		}
	}


	public boolean isAlive() {
		return alive;
	}


	public float getHealth() {
		return this.health;
	}


	public void setHealth(float h) {
		this.health = h;
	}


	protected void decHealth(float h) {
		this.health -= h;
		if (health < 0) {
			health = 0;
		}
	}


	@Override
	public Collidable getCollidable() {
		return this.getMainNode().getWorldBound();
	}


	public IAbility getAbility(int eid) {
		for (int i=0 ; i< this.ability.length ; i++) {
			if (this.ability[i] != null && this.ability[i].getID() == eid) {
				return this.ability[i];
			}
		}
		return null;
	}

}
