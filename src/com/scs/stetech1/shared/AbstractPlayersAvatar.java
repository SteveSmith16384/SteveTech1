package com.scs.stetech1.shared;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.scs.stetech1.client.MyBetterCharacterControl;
import com.scs.stetech1.client.entities.PhysicalEntity;
import com.scs.stetech1.components.IAffectedByPhysics;
import com.scs.stetech1.components.IBullet;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.components.ICollideable;
import com.scs.stetech1.components.IDamagable;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.components.IProcessable;
import com.scs.stetech1.components.IShowOnHUD;
import com.scs.stetech1.input.IInputDevice;
import com.scs.stetech1.server.ServerMain;
import com.scs.stetech1.server.Settings;

public abstract class AbstractPlayersAvatar extends PhysicalEntity implements IProcessable, ICollideable, ICanShoot, IShowOnHUD, IAffectedByPhysics, IDamagable {

	// Player dimensions
	public static final float PLAYER_HEIGHT = 0.7f;
	public static final float PLAYER_RAD = 0.2f;
	private static final float WEIGHT = 3f;

	public final Vector3f walkDirection = new Vector3f();
	public float moveSpeed = Settings.PLAYER_MOVE_SPEED;
	protected IInputDevice input;

	//Temporary vectors used on each frame.
	public final Vector3f camDir = new Vector3f();
	private final Vector3f camLeft = new Vector3f();

	public MyBetterCharacterControl playerControl;
	public final int playerID;
	public Spatial playerGeometry;
	private float score = 0;
	private float health;

	protected boolean restarting = false;
	protected float restartTime, invulnerableTime;

	private int numShots = 0;
	private int numShotsHit = 0;

	public AbstractPlayersAvatar(IEntityController _module, int _playerID, IInputDevice _input) {
		super(_module, EntityTypes.AVATAR, "Player");

		playerID = _playerID;
		input = _input;
		//health = module.getPlayersHealth(id);

		//int pid = 1;//Settings.GAME_MODE != GameMode.CloneWars ? id : Settings.CLONE_ID;
		playerGeometry = getPlayersModel(module, playerID);
		this.getMainNode().attachChild(playerGeometry);

		playerControl = new MyBetterCharacterControl(PLAYER_RAD, PLAYER_HEIGHT, WEIGHT);
		playerControl.setJumpForce(new Vector3f(0, Settings.JUMP_FORCE, 0)); 
		this.getMainNode().addControl(playerControl);
		
		module.getBulletAppState().getPhysicsSpace().add(playerControl);
		module.getRootNode().attachChild(this.main_node);

		this.getMainNode().setUserData(Settings.ENTITY, this);
		playerControl.getPhysicsRigidBody().setUserObject(this);

		/*abilityGun = new LaserRifle(_game, _module, this);
		if (Settings.DEBUG_SPELLS) {
			this.abilityOther = new Spellbook(module, this);
		} else {
			this.abilityOther = new JetPac(this);// BoostFwd(this);//getRandomAbility(this);
		}*/

		/*this.hud.setAbilityGunText(this.abilityGun.getHudText());
		if (abilityOther != null) {
			this.hud.setAbilityOtherText(this.abilityOther.getHudText());
		}*/

		playerControl.getPhysicsRigidBody().setCcdMotionThreshold(PLAYER_RAD*2);

	}


	public static Spatial getPlayersModel(IEntityController game, int pid) {
		// Add player's box
		Box box1 = new Box(PLAYER_RAD, PLAYER_HEIGHT/2, PLAYER_RAD);
		//Cylinder box1 = new Cylinder(1, 8, PLAYER_RAD, PLAYER_HEIGHT, true);
		Geometry playerGeometry = new Geometry("Player", box1);
		TextureKey key3 = new TextureKey("Textures/neon1.jpg");
		key3.setGenerateMips(true);
		Texture tex3 = game.getAssetManager().loadTexture(key3);
		Material floor_mat = null;
		if (Settings.LIGHTING) {
			floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
			floor_mat.setTexture("DiffuseMap", tex3);
		} else {
			floor_mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
			floor_mat.setTexture("ColorMap", tex3);
		}
		playerGeometry.setMaterial(floor_mat);
		//playerGeometry.setLocalTranslation(new Vector3f(0, PLAYER_HEIGHT/2, 0)); // Need this to ensure the crate is on the floor
		playerGeometry.setLocalTranslation(new Vector3f(0, (PLAYER_HEIGHT/2)-.075f, 0)); // Need this to ensure the crate is on the floor
		return playerGeometry;
	}


	/*private static IAbility getRandomAbility(PlayersAvatar _player) {
		int i = NumberFunctions.rnd(1, 3);
		switch (i) {
		case 1:
			return new JetPac(_player);
		case 2:
			return new Invisibility(_player);
		case 3:
			return new RunFast(_player);
		default:
			throw new RuntimeException("Unknown ability: " + i);
		}

	}*/


	@Override
	public void process(float tpf) {
		if (invulnerableTime >= 0) {
			invulnerableTime -= tpf;
		}

		if (!this.restarting) {
			// Have we fallen off the edge
			if (this.playerControl.getPhysicsRigidBody().getPhysicsLocation().y < -5f) { // scs catching here after died!
				died("Too low");
				return;
			}

			/*abilityGun.process(tpf);
			if (this.abilityOther != null) {
				abilityOther.process(tpf);
			}
			 */

			/*if (this.abilityOther != null) {
				if (input.isAbilityOtherPressed()) { // Must be before we set the walkDirection & moveSpeed, as this method may affect it
					//Settings.p("Using " + this.ability.toString());
					this.abilityOther.activate(tpf);
				}
			}*/

			/*
			 * The direction of character is determined by the camera angle
			 * the Y direction is set to zero to keep our character from
			 * lifting of terrain. For free flying games simply add speed 
			 * to Y axis
			 */
			camDir.set(input.getDirection()).multLocal(moveSpeed, 0.0f, moveSpeed);
			camLeft.set(input.getLeft()).multLocal(moveSpeed);
			if (input.getFwdValue()) {	
				//Settings.p("fwd=" + input.getFwdValue());
				walkDirection.addLocal(camDir);
			} else if (input.getBackValue()) {
				walkDirection.addLocal(camDir.negate());
			}
			if (input.getStrafeLeftValue()) {		
				walkDirection.addLocal(camLeft);
			} else if (input.getStrafeRightValue()) {		
				walkDirection.addLocal(camLeft.negate());
			}

			/*if (walkDirection.length() != 0) {
				Settings.p("walkDirection=" + walkDirection);
			}*/
			playerControl.setWalkDirection(walkDirection);

			if (input.isJumpPressed()){
				this.jump();
			}

			if (input.isShootPressed()) {
				shoot();
			}

			// These must be after we might use them, so the hud is correct 
			/*this.hud.setAbilityGunText(this.abilityGun.getHudText());
			if (abilityOther != null) {
				this.hud.setAbilityOtherText(this.abilityOther.getHudText());
			}*/

		}

		//this.input.resetFlags();

		walkDirection.set(0, 0, 0);
	}

	
	/*public abstract Vector3f getDirection();

	public abstract Vector3f getLeft();*/

	public boolean isOnGround() {
		return playerControl.isOnGround();
	}


	public void shoot() {
		/*if (this.abilityGun.activate(0)) {
			this.score--;
			this.hud.setScore(this.score);
			this.numShots++;
			calcAccuracy();
		}*/
	}


	public void jump() {
		this.playerControl.jump();
	}


	public void hitByBullet(IBullet bullet) {
		if (invulnerableTime <= 0) {
			float dam = bullet.getDamageCaused();
			if (dam > 0) {
				Settings.p("Player hit by bullet");
				//module.doExplosion(this.main_node.getWorldTranslation(), this);
				//module.audioExplode.play();
				//this.health -= dam;
				//this.hud.setHealth(this.health);
				//this.hud.showDamageBox();

				died("hit by " + bullet.toString());
			}
		} else {
			Settings.p("Player hit but is currently invulnerable");
		}
	}


	private void died(String reason) {
		Settings.p("Player died: " + reason);
		this.restarting = true;
		this.restartTime = ServerMain.properties.GetRestartTimeSecs();
		//invulnerableTime = RESTART_DUR*3;

		// Move us below the map
		Vector3f pos = this.getMainNode().getWorldTranslation().clone();//.floor_phy.getPhysicsLocation().clone();
		pos.y = -10;//-SimpleCity.FLOOR_THICKNESS * 2;
		playerControl.warp(pos);
	}


	@Override
	public void hasSuccessfullyHit(IEntity e) {
		this.incScore(20, "shot " + e.toString());
		//new AbstractHUDImage(game, module, this.hud, "Textures/text/hit.png", this.hud.hud_width, this.hud.hud_height, 2);
		//this.hud.showCollectBox();
		numShotsHit++;
		calcAccuracy();
	}


	private void calcAccuracy() {
		int a = (int)((this.numShotsHit * 100f) / this.numShots);
		//hud.setAccuracy(a);
	}


	public void incScore(float amt, String reason) {
		Settings.p("Inc score: +" + amt + ", " + reason);
		this.score += amt;
		//this.hud.setScore(this.score);

	}


	@Override
	public void collidedWith(ICollideable other) {
		if (other instanceof IBullet) {
			IBullet bullet = (IBullet)other;
			if (bullet.getShooter() != null) {
				if (bullet.getShooter() != this) {
					if (!(bullet.getShooter() instanceof AbstractPlayersAvatar)) {
						this.hitByBullet(bullet);
						bullet.getShooter().hasSuccessfullyHit(this);
					}
				}
			}
		}
	}


	@Override
	public void applyForce(Vector3f force) {
		//playerControl.getPhysicsRigidBody().applyImpulse(force, Vector3f.ZERO);//.applyCentralForce(dir);
		//playerControl.getPhysicsRigidBody().applyCentralForce(force);
		//Settings.p("Applying force to player:" + force);
		//this.addWalkDirection.addLocal(force);
	}


	@Override
	public void damaged(float amt, String reason) {
		died(reason);
	}



	@Override
	public void remove() {
		super.remove();
		this.module.getBulletAppState().getPhysicsSpace().remove(this.playerControl);

	}


	@Override
	public HashMap<String, Object> getCreationData() {
		return null;
	}


	@Override
	public boolean canMove() {
			return true;
	}

}
