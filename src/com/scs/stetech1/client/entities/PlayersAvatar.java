package com.scs.stetech1.client.entities;

import java.util.List;

import com.jme3.asset.TextureKey;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Camera.FrustumIntersect;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.scs.stetech1.client.MyBetterCharacterControl;
import com.scs.stetech1.components.IAffectedByPhysics;
import com.scs.stetech1.components.IBullet;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.components.ICollideable;
import com.scs.stetech1.components.IDamagable;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.components.IProcessable;
import com.scs.stetech1.components.IShowOnHUD;
import com.scs.stetech1.components.ITargetByAI;
import com.scs.stetech1.hud.AbstractHUDImage;
import com.scs.stetech1.hud.HUD;
import com.scs.stetech1.input.IInputDevice;
import com.scs.stetech1.server.ServerMain;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.IEntityController;

public class PlayersAvatar extends PhysicalEntity implements IProcessable, ICollideable, ICanShoot, IShowOnHUD, ITargetByAI, IAffectedByPhysics, IDamagable {

	// Player dimensions
	public static final float PLAYER_HEIGHT = 0.7f;
	public static final float PLAYER_RAD = 0.2f;
	private static final float WEIGHT = 3f;

	public final Vector3f walkDirection = new Vector3f();
	public float moveSpeed = Settings.PLAYER_MOVE_SPEED;
	private IInputDevice input;

	//Temporary vectors used on each frame.
	public Camera cam;
	public final Vector3f camDir = new Vector3f();
	private final Vector3f camLeft = new Vector3f();

	public HUD hud;
	public MyBetterCharacterControl playerControl;
	public final int id;
	public Spatial playerGeometry;
	private float score = 0;
	private float health;

	private boolean restarting = false;
	private float restartTime, invulnerableTime;

	private int numShots = 0;
	private int numShotsHit = 0;

	public PlayersAvatar(IEntityController _module, int _id, Camera _cam, IInputDevice _input, HUD _hud) {
		super(_module, "Player");

		id = _id;
		cam = _cam;
		input = _input;
		hud = _hud;
		//health = module.getPlayersHealth(id);

		int pid = 1;//Settings.GAME_MODE != GameMode.CloneWars ? id : Settings.CLONE_ID;
		playerGeometry = getPlayersModel(module, pid);
		this.getMainNode().attachChild(playerGeometry);

		playerControl = new MyBetterCharacterControl(PLAYER_RAD, PLAYER_HEIGHT, WEIGHT);
		playerControl.setJumpForce(new Vector3f(0, Settings.JUMP_FORCE, 0)); 
		this.getMainNode().addControl(playerControl);
		module.getBulletAppState().getPhysicsSpace().add(playerControl);

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
			TextureKey key3 = new TextureKey("Textures/computerconsole2.jpg");
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


	public void moveToStartPostion(boolean invuln) {
		/*Point p = module.mapData.getPlayerStartPos(id);
		Vector3f warpPos = new Vector3f(p.x, module.mapData.getRespawnHeight(), p.y);
		Settings.p("Scheduling player to start position: " + warpPos);
		this.playerControl.warp(warpPos);
		if (invuln) {
			invulnerableTime = Sorcerers.properties.GetInvulnerableTimeSecs();
		}*/
	}


	@Override
	public void process(float tpf) {
		if (this.restarting) {
			restartTime -= tpf;
			if (this.restartTime <= 0) {
				this.moveToStartPostion(true);
				restarting = false;
				return;
			}
		}

		if (invulnerableTime >= 0) {
			invulnerableTime -= tpf;
		}

		if (!this.restarting) {
			// Have we fallen off the edge
			if (this.playerControl.getPhysicsRigidBody().getPhysicsLocation().y < -1f) { // scs catching here after died!
				died("Too low");
				return;
			}

			/*abilityGun.process(tpf);
			if (this.abilityOther != null) {
				abilityOther.process(tpf);
			}
*/
			hud.process(tpf);

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
			camDir.set(cam.getDirection()).multLocal(moveSpeed, 0.0f, moveSpeed);
			camLeft.set(cam.getLeft()).multLocal(moveSpeed);
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

		// Position camera at node
		Vector3f vec = getMainNode().getWorldTranslation();
		cam.setLocation(new Vector3f(vec.x, vec.y + (PLAYER_HEIGHT/2), vec.z));

		// Rotate us to point in the direction of the camera
		Vector3f lookAtPoint = cam.getLocation().add(cam.getDirection().mult(10));
		//gun.lookAt(lookAtPoint.clone(), Vector3f.UNIT_Y);
		lookAtPoint.y = cam.getLocation().y; // Look horizontal
		this.playerGeometry.lookAt(lookAtPoint, Vector3f.UNIT_Y);
		//this.getMainNode().lookAt(lookAtPoint.clone(), Vector3f.UNIT_Y);  This won't rotate the model since it's locked to the physics controller

		// Move cam fwd so we don't see ourselves
		cam.setLocation(cam.getLocation().add(cam.getDirection().mult(PLAYER_RAD)));
		cam.update();

		//this.input.resetFlags();

		walkDirection.set(0, 0, 0);
	}


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


	public FrustumIntersect getInsideOutside(PhysicalEntity entity) {
		FrustumIntersect insideoutside = cam.contains(entity.getMainNode().getWorldBound());
		return insideoutside;
	}


	@Override
	public Vector3f getLocation() {
		return this.cam.getLocation();
		//return playerControl.getPhysicsRigidBody().getPhysicsLocation();  This is very low to the ground!
	}


	@Override
	public Vector3f getShootDir() {
		return this.cam.getDirection();
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
				this.hud.showDamageBox();

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
		this.hud.showCollectBox();
		numShotsHit++;
		calcAccuracy();
	}


	private void calcAccuracy() {
		int a = (int)((this.numShotsHit * 100f) / this.numShots);
		hud.setAccuracy(a);
	}


	public void incScore(float amt, String reason) {
		Settings.p("Inc score: +" + amt + ", " + reason);
		this.score += amt;
		this.hud.setScore(this.score);

		if (this.score >= 100) {
			new AbstractHUDImage(module, this.hud, "Textures/text/winner.png", this.hud.hud_width, this.hud.hud_height, 10);
		}
	}


	@Override
	public void collidedWith(ICollideable other) {
		if (other instanceof IBullet) {
			IBullet bullet = (IBullet)other;
			if (bullet.getShooter() != null) {
				if (bullet.getShooter() != this) {
					if (!(bullet.getShooter() instanceof PlayersAvatar)) {
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


	public Camera getCamera() {
		return this.cam;
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


	public Vector3f getPointOnFloor(float range) {
		Vector3f from = this.cam.getLocation();
		Vector3f to = this.cam.getDirection().normalize().multLocal(range).addLocal(from);
		List<PhysicsRayTestResult> results = module.getBulletAppState().getPhysicsSpace().rayTest(from, to);
		float dist = -1;
		PhysicsRayTestResult closest = null;
		for (PhysicsRayTestResult r : results) {
			if (r.getCollisionObject().getUserObject() != null) {
				if (closest == null) {
					closest = r;
				} else if (r.getHitFraction() < dist) {
					closest = r;
				}
				dist = r.getHitFraction();
			}
		}
		if (closest != null) {
			Entity e = (Entity)closest.getCollisionObject().getUserObject();
			Vector3f hitpoint = to.subtract(from).multLocal(closest.getHitFraction()).addLocal(from);
			Settings.p("Hit " + e + " at " + hitpoint);
			//module.doExplosion(from, null);
			return hitpoint;
		}

		return null;
	}

}
