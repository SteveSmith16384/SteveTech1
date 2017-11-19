package com.scs.stetech1.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.scs.stetech1.IAbility;
import com.scs.stetech1.client.GenericClient;
import com.scs.stetech1.components.IAffectedByPhysics;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.components.IProcessByServer;
import com.scs.stetech1.input.IInputDevice;
import com.scs.stetech1.jme.MyBetterCharacterControl;
import com.scs.stetech1.server.ServerMain;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityTypes;
import com.scs.stetech1.shared.IEntityController;
import com.scs.stetech1.weapons.HitscanRifle;

public abstract class AbstractPlayersAvatar extends PhysicalEntity implements IProcessByServer, ICanShoot, IAffectedByPhysics {

	// Player dimensions - todo - move these
	public static final float PLAYER_HEIGHT = 0.7f;
	public static final float PLAYER_RAD = 0.2f;
	private static final float WEIGHT = 3f;

	private final Vector3f walkDirection = new Vector3f();
	public final float moveSpeed = Settings.PLAYER_MOVE_SPEED;
	protected IInputDevice input;

	//Temporary vectors used on each frame.
	private final Vector3f camDir = new Vector3f();
	private final Vector3f camLeft = new Vector3f();

	//public MyBetterCharacterControl2 playerControl;
	public MyBetterCharacterControl playerControl;
	public final int playerID;
	public Spatial playerGeometry;
	protected float health;
	protected boolean restarting = false;
	protected float restartTime, invulnerableTime;
	private int numShots = 0;
	private int numShotsHit = 0;
	public IAbility abilityGun, abilityOther;


	public AbstractPlayersAvatar(IEntityController _game, int _playerID, IInputDevice _input, int eid) {
		super(_game, eid, EntityTypes.AVATAR, "Player");

		if (game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("id", eid); this.getID();
			creationData.put("playerID", _playerID);
		}

		playerID = _playerID;
		input = _input;

		playerGeometry = getPlayersModel(game, playerID);
		playerGeometry.setCullHint(CullHint.Always); // Don't draw ourselves - yet?

		this.getMainNode().attachChild(playerGeometry);

		playerControl = new MyBetterCharacterControl(PLAYER_RAD, PLAYER_HEIGHT, WEIGHT);
		playerControl.setJumpForce(new Vector3f(0, Settings.JUMP_FORCE, 0)); 
		this.getMainNode().addControl(playerControl);

		game.getBulletAppState().getPhysicsSpace().add(playerControl);
		game.getRootNode().attachChild(this.main_node);

		this.getMainNode().setUserData(Settings.ENTITY, this);
		playerControl.getPhysicsRigidBody().setUserObject(this);

		abilityGun = new HitscanRifle(game, this);
		/* 
			this.abilityOther = new JetPac(this);// BoostFwd(this);//getRandomAbility(this);
		}*/

		/*this.hud.setAbilityGunText(this.abilityGun.getHudText());
		if (abilityOther != null) {
			this.hud.setAbilityOtherText(this.abilityOther.getHudText());
		}*/

	}


	public static Spatial getPlayersModel(IEntityController game, int pid) {
		Box box1 = new Box(PLAYER_RAD, PLAYER_HEIGHT/2, PLAYER_RAD);
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
		return playerGeometry;
	}


	protected void serverAndClientProcess(ServerMain server, GenericClient client, float tpf) {
		if (game.isServer()) { // Client does it before adjusting
			this.resetWalkDir(); // todo - do this in one place
		}

		abilityGun.process(server, tpf);
		if (this.abilityOther != null) {
			abilityOther.process(server, tpf);
		}

		if (this.abilityOther != null) {
			if (input.isAbilityOtherPressed()) { // Must be before we set the walkDirection & moveSpeed, as this method may affect it
				//Settings.p("Using " + this.ability.toString());
				this.abilityOther.activate(tpf);
			}
		}

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
		if (input.isJumpPressed()){
			this.jump();
		}
		if (input.isShootPressed()) {
			shoot();
		}

		playerControl.setWalkDirection(walkDirection.clone()); // todo - need to clone?

		// These must be after we might use them, so the hud is correct 
		/*this.hud.setAbilityGunText(this.abilityGun.getHudText());
			if (abilityOther != null) {
				this.hud.setAbilityOtherText(this.abilityOther.getHudText());
			}*/

	}


	public boolean isOnGround() {
		return playerControl.isOnGround();
	}


	public void addToWalkDir(Vector3f offset) {
		this.walkDirection.addLocal(offset);
	}


	public void resetWalkDir() {
		this.walkDirection.set(0, 0, 0);
	}


	public void shoot() {
		if (this.abilityGun.activate(0)) {
			this.numShots++;
		}
	}



	public void jump() {
		//if (this.game.isServer()) { // Let the server do it, and the client copy
		Settings.p("Jumping!");
		this.playerControl.jump();
		//}
	}


	@Override
	public void applyForce(Vector3f force) {
		//playerControl.getPhysicsRigidBody().applyImpulse(force, Vector3f.ZERO);//.applyCentralForce(dir);
		//playerControl.getPhysicsRigidBody().applyCentralForce(force);
		Settings.p("Unable to apply force to player:" + force);
		//this.addWalkDirection.addLocal(force);
	}


	@Override
	public void remove() {
		super.remove();
		this.game.getBulletAppState().getPhysicsSpace().remove(this.playerControl);

	}


	@Override
	public Vector3f getWorldTranslation() {
		// Need this override since main node is at 0,0,0 at the start
		return this.playerControl.getPhysicsRigidBody().getPhysicsLocation();
		//return this.main_node.getWorldTranslation(); 000?
		//return this.getMainNode().getLocalTranslation();
	}


	// Do NOT use this to "tweak" players position!
	@Override
	public void setWorldTranslation(Vector3f pos) {
		//float dist = pos.distance(this.getWorldTranslation());
		// We need to warp() players
		this.playerControl.warp(pos);
	}


	@Override
	public boolean canMove() {
		return true; // Always calc for avatars
	}


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
		this.walkDirection.addLocal(offset);//.multLocal(moveSpeed)); 
		//}
	}


	@Override
	public Vector3f getBulletStartOffset() {
		Vector3f offset = this.getShootDir().multLocal(AbstractPlayersAvatar.PLAYER_RAD*3);
		offset.y -= 0.1f; // Drop bullets slightly
		return offset;
	}


}