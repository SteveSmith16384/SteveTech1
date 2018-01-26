package com.scs.stevetech1.entities;

import java.util.HashMap;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.scs.simplephysics.SimpleCharacterControl;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IAffectedByPhysics;
import com.scs.stevetech1.components.IAnimatedAvatarModel;
import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IPreprocess;
import com.scs.stevetech1.components.IProcessByServer;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;

public abstract class AbstractAvatar extends PhysicalEntity implements IPreprocess, IProcessByServer, ICanShoot, IAffectedByPhysics {

	private final Vector3f walkDirection = new Vector3f(); // Need sep walkDir as we set y=0 on this one, but not the one in RigidBody
	public final float moveSpeed = Globals.PLAYER_MOVE_SPEED;
	protected IInputDevice input;

	//Temporary vectors used on each frame.
	private final Vector3f camDir = new Vector3f();
	private final Vector3f camLeft = new Vector3f();

	public final int playerID;
	public Spatial playerGeometry;
	protected float health;
	private int numShots = 0;
	private int numShotsHit = 0;
	public IAbility abilityGun, abilityOther; // todo - have list of abilities
	public int side = -1;
	protected IAnimatedAvatarModel avatarModel;

	public boolean alive = true;
	protected float restartTime, invulnerableTime;

	public AbstractAvatar(IEntityController _game, int _playerID, IInputDevice _input, int eid, int _side, IAnimatedAvatarModel _anim) {
		super(_game, eid, 1, "Player", true);

		if (game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("id", eid); this.getID();
			creationData.put("playerID", _playerID);
			creationData.put("side", _side);
		}

		playerID = _playerID;
		input = _input;
		side =_side;
		avatarModel = _anim;

		playerGeometry = avatarModel.getModel(!game.isServer());// getPlayersModel(game, playerID);
		playerGeometry.setCullHint(CullHint.Always); // Don't draw ourselves - yet?

		this.getMainNode().attachChild(playerGeometry);

		this.simpleRigidBody = new SimpleCharacterControl<PhysicalEntity>(this.mainNode, game.getPhysicsController(), this);

		game.getRootNode().attachChild(this.mainNode);

		playerGeometry.setUserData(Globals.ENTITY, this);
		this.getMainNode().setUserData(Globals.ENTITY, this);

		/*this.hud.setAbilityGunText(this.abilityGun.getHudText());
		if (abilityOther != null) {
			this.hud.setAbilityOtherText(this.abilityOther.getHudText());
		}*/

	}


	//protected abstract Spatial getPlayersModel(IEntityController game, int pid);


	protected void serverAndClientProcess(AbstractGameServer server, AbstractGameClient client, float tpf_secs, long serverTime) {
		this.resetWalkDir();

		// Reset addition force
		/*if (server != null) { // Only do this server-side, since it contains sync adjustments
			simplePlayerControl.getAdditionalForce().set(0, 0, 0);
		}*/

		if (this.abilityOther != null) {
			if (input.isAbilityOtherPressed()) { // Must be before we set the walkDirection & moveSpeed, as this method may affect it
				//Settings.p("Using " + this.ability.toString());
				this.abilityOther.activate();
			}
		}

		this.currentAnim = avatarModel.getAnimationStringForCode("Idle"); // Default
		camDir.set(input.getDirection()).multLocal(moveSpeed, 0.0f, moveSpeed);
		camLeft.set(input.getLeft()).multLocal(moveSpeed);
		if (input.getFwdValue()) {
			//Settings.p("fwd=" + input.getFwdValue());
			walkDirection.addLocal(camDir);  //this.getMainNode().getWorldTranslation();
			this.currentAnim = avatarModel.getAnimationStringForCode("Walking");
		} else if (input.getBackValue()) {
			walkDirection.addLocal(camDir.negate());
			this.currentAnim = avatarModel.getAnimationStringForCode("Walking");
		}
		if (input.getStrafeLeftValue()) {		
			walkDirection.addLocal(camLeft);
			this.currentAnim = avatarModel.getAnimationStringForCode("Walking");
		} else if (input.getStrafeRightValue()) {		
			walkDirection.addLocal(camLeft.negate());
			this.currentAnim = avatarModel.getAnimationStringForCode("Walking");
		}
		if (input.isJumpPressed()){
			this.jump();
		}
		if (input.isShootPressed()) {
			shoot();
		}

		if (this.walkDirection.length() != 0) {
			if (!this.game.isServer() || Globals.STOP_SERVER_AVATAR_MOVING == false) {
				SimpleCharacterControl<PhysicalEntity> simplePlayerControl = (SimpleCharacterControl<PhysicalEntity>)this.simpleRigidBody; 
				simplePlayerControl.getAdditionalForce().addLocal(walkDirection);
				if (Globals.SHOW_AVATAR_WALK_DIR) {
					Globals.p("time=" + serverTime + ",   pos=" + this.getWorldTranslation());// + "  walkDirection=" + walkDirection);
				}
			}
		}

		// These must be after we might use them, so the hud is correct 
		/*this.hud.setAbilityGunText(this.abilityGun.getHudText());
			if (abilityOther != null) {
				this.hud.setAbilityOtherText(this.abilityOther.getHudText());
			}*/

		simpleRigidBody.process(tpf_secs);

		// Point us in the right direction
		if (this.game.isServer()) {
			Vector3f lookAtPoint = camLeft.add(camDir.mult(10));
			lookAtPoint.y = this.getMainNode().getWorldTranslation().y; // Look horizontal!
			this.getMainNode().lookAt(lookAtPoint, Vector3f.UNIT_Y); // need this in order to send the avatar's rotation to other players
		}
	}



	public void addToWalkDir(Vector3f offset) {
		this.walkDirection.addLocal(offset);
	}


	public void resetWalkDir() {
		this.walkDirection.set(0, 0, 0);
	}


	public void shoot() {
		if (this.abilityGun.activate()) {
			this.currentAnim = avatarModel.getAnimationStringForCode(abilityGun.getAvatarAnimationCode());
			this.numShots++;
		}
	}



	public void jump() {
		Globals.p("Jumping!");
		//if (this.game.isServer()) { Too much of a delay
		SimpleCharacterControl<PhysicalEntity> simplePlayerControl = (SimpleCharacterControl<PhysicalEntity>)this.simpleRigidBody; 
		simplePlayerControl.jump();
		//}
	}


	/*@Override
	public boolean canMove() {
		return true; // Always calc for avatars
	}
	 */

	@Override
	public boolean hasMoved() {
		return true; // Always send for avatars
	}


	public boolean isShooting() {
		return this.input.isShootPressed();
	}


	/*
	 * Need this since we can't warp a player to correct their position, as they may warp into walls!
	 * Also, we're adjusting their position based on the past, so we want to offset them, rather than move them to
	 * a specific point
	 */
	@Override
	public void adjustWorldTranslation(Vector3f offset) { // Adjust avatars differently to normal entities
		//if (offset.length() > 0.01f) { Already checked this
		SimpleCharacterControl<PhysicalEntity> simplePlayerControl = (SimpleCharacterControl<PhysicalEntity>)this.simpleRigidBody;
		simplePlayerControl.getAdditionalForce().addLocal(offset);
		//}
	}


	@Override
	public int getSide() {
		return side;
	}


	public void addAbility(IAbility a, int num) {
		if (num == 0) {
			this.abilityGun = a;
		} else if (num == 1) {
			this.abilityOther = a;
		} else {
			throw new RuntimeException("Unknown ability num: " + num);
		}
	}


	@Override
	public void preprocess() {
		SimpleCharacterControl<PhysicalEntity> simplePlayerControl = (SimpleCharacterControl<PhysicalEntity>)this.simpleRigidBody;
		simplePlayerControl.getAdditionalForce().set(0, 0, 0);
	}


	@Override
	public Vector3f getBulletStartPos() {
		return this.getWorldTranslation().add(0, avatarModel.getBulletStartHeight(), 0);
	}


	@Override
	public void remove() {
		if (abilityGun != null) {
			abilityGun.remove();
		}
		if (abilityOther != null) {
			abilityOther.remove();
		}
		super.remove();
	}
}
