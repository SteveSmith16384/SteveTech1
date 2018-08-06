package com.scs.stevetech1.avatartypes;

import com.jme3.math.Vector3f;
import com.scs.simplephysics.SimpleCharacterControl;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class PersonAvatar implements IAvatarControl { // todo - rename

	protected IEntityController game;
	private IInputDevice input;
	private final Vector3f walkDirection = new Vector3f(); // Need sep walkDir as we set y=0 on this one, but not the one in RigidBody

	//Temporary vectors used on each frame.
	private final Vector3f camDir = new Vector3f();
	private final Vector3f camLeft = new Vector3f();

	protected long lastMoveTime = System.currentTimeMillis() + 5000;
	protected boolean playerWalked; // Has the player tried to move us?
	public float moveSpeed = 0f;
	private float jumpForce = 0;
	private int newAnimCode;
	private SimpleRigidBody<PhysicalEntity> simpleRigidBody;

	public PersonAvatar(IEntityController _game, IInputDevice _input, float _moveSpeed, float _jumpForce) {
		game = _game;
		input = _input;
		this.moveSpeed = _moveSpeed;
		this.jumpForce = _jumpForce;
	}
	
		
	@Override
	public void init(PhysicalEntity pe) {
		simpleRigidBody = new SimpleCharacterControl<PhysicalEntity>(pe, game.getPhysicsController(), pe);
	}
	
	
	@Override
	public SimpleRigidBody<PhysicalEntity> getSimpleRigidBody() {
		return simpleRigidBody;
	}


	@Override
	public void process() {
		this.resetWalkDir();

		newAnimCode = AbstractAvatar.ANIM_IDLE; // Default

		camDir.set(input.getDirection()).multLocal(moveSpeed, 0.0f, moveSpeed); // Y=0, so speed is constant regardless of direction
		camLeft.set(input.getLeft()).multLocal(moveSpeed);

		//if (this.isAlive()) {
		if (input.getFwdValue()) {
			walkDirection.addLocal(camDir);  //this.getMainNode().getWorldTranslation();
			newAnimCode = AbstractAvatar.ANIM_RUNNING;
			lastMoveTime = System.currentTimeMillis();
		} else if (input.getBackValue()) {
			walkDirection.addLocal(camDir.negate());
			newAnimCode = AbstractAvatar.ANIM_RUNNING;
			lastMoveTime = System.currentTimeMillis();
		}
		if (input.getStrafeLeftValue()) {		
			walkDirection.addLocal(camLeft);
			newAnimCode = AbstractAvatar.ANIM_RUNNING;
			lastMoveTime = System.currentTimeMillis();
		} else if (input.getStrafeRightValue()) {		
			walkDirection.addLocal(camLeft.negate());
			newAnimCode = AbstractAvatar.ANIM_RUNNING;
			lastMoveTime = System.currentTimeMillis();
		}
		if (input.isJumpPressed()) {
			if (this.jump()) {
				newAnimCode = AbstractAvatar.ANIM_JUMP;
			}
		}

		playerWalked = false;
		if (this.walkDirection.length() != 0) {
			if (!this.game.isServer() || Globals.STOP_SERVER_AVATAR_MOVING == false) {
				SimpleCharacterControl<PhysicalEntity> simplePlayerControl = (SimpleCharacterControl<PhysicalEntity>)this.simpleRigidBody; 
				simplePlayerControl.getAdditionalForce().addLocal(walkDirection);
			}
			playerWalked = true;
		}
		//}

		
	}
	
	
	protected void resetWalkDir() {
		this.walkDirection.set(0, 0, 0);
	}


	@Override
	public boolean jump() {
		SimpleCharacterControl<PhysicalEntity> simplePlayerControl = (SimpleCharacterControl<PhysicalEntity>)this.simpleRigidBody; 
		if (simplePlayerControl.jump()) {
			lastMoveTime = System.currentTimeMillis();
			return true;
		}
		return false;
	}


	@Override
	public int getCurrentAnimCode() {
		return this.newAnimCode;
	}


	@Override
	public long getLastMoveTime() {
		return this.lastMoveTime;
	}


}
