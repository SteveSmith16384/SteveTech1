package com.scs.stevetech1.entities;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.simplephysics.SimpleCharacterControl;
import com.scs.stevetech1.avatartypes.IAvatarControl;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.client.HistoricalPositionCalculator;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IAvatarModel;
import com.scs.stevetech1.components.IKillable;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.components.IShowOnHUD;
import com.scs.stevetech1.hud.IHUD;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.netmessages.AbilityActivatedMessage;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.EntityPositionData;
import com.scs.stevetech1.shared.PositionCalculator;

/**
 * This is the superclass for the client-side players avatar.
 *
 */
public abstract class AbstractClientAvatar extends AbstractAvatar implements IShowOnHUD, IProcessByClient, IKillable {

	public IHUD hud;
	public Camera cam;
	public PositionCalculator clientAvatarPositionData = new PositionCalculator(Globals.HISTORY_DURATION); // So we know where we were in the past to compare against where the server says we should have been
	private Spatial debugNode;
	public PhysicalEntity killer;

	public AbstractClientAvatar(AbstractGameClient _client, int avatarType, int _playerID, IInputDevice _input, Camera _cam, IHUD _hud, int eid, 
			float x, float y, float z, int side, IAvatarModel avatarModel, IAvatarControl _avatarControl) {
		super(_client, avatarType, _playerID, _input, eid, side, avatarModel, _avatarControl);

		cam = _cam;
		hud = _hud;
		//moveSpeed = _moveSpeed;
		//this.setJumpForce(_jumpSpeed);

		this.setWorldTranslation(new Vector3f(x, y, z));

		if (Globals.SHOW_SERVER_AVATAR_ON_CLIENT) {
			createDebugBox();
		}
	}


	private void createDebugBox() {
		//BoundingBox bb = (BoundingBox)super.playerGeometry.getWorldBound();
		//Box box1 = new Box(bb.getXExtent()*2, bb.getYExtent()*2, bb.getZExtent()*2);
		Box box1 = new Box(.5f, .1f, .5f);
		debugNode = new Geometry("DebugBox", box1);
		TextureKey key3 = new TextureKey("Textures/neon1.jpg");
		key3.setGenerateMips(true);
		Texture tex3 = game.getAssetManager().loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);

		Material floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
		floor_mat.setTexture("DiffuseMap", tex3);
		debugNode.setMaterial(floor_mat);

		debugNode.setLocalTranslation(0, box1.yExtent/2, 0); // Origin is at the bottom

		game.getGameNode().attachChild(debugNode);

	}


	@Override
	public void remove() {
		super.remove();
		if (Globals.SHOW_SERVER_AVATAR_ON_CLIENT) {
			this.debugNode.removeFromParent();
		}
	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		if (Globals.DEBUG_SET_ALIVE) {
			this.alive = false;
		}

		if (!this.alive) {
			if (Globals.SHOW_VIEW_FROM_KILLER_ON_DEATH && this.killer != null) {
				Vector3f vec = killer.getWorldTranslation();
				cam.getLocation().x = vec.x;
				cam.getLocation().y = vec.y + avatarModel.getCameraHeight();
				cam.getLocation().z = vec.z;

				cam.lookAt(this.getWorldTranslation(), Vector3f.UNIT_Y);

			} else {
				// Position cam above avatar when they're dead
				Vector3f vec = this.getWorldTranslation();
				cam.getLocation().y = vec.y + 0.1f;

				if (this.killer != null) {
					cam.lookAt(killer.getWorldTranslation(), Vector3f.UNIT_Y);
				}
			}
			cam.update();

		} else {
			final long serverTime = client.getServerTime();

			// Check for any abilities/guns being fired
			if (this.input != null) {
				for (int i=0 ; i<this.ability.length ; i++) {
					if (this.ability[i] != null) {
						if (input.isAbilityPressed(i)) { // Must be before we set the walkDirection & moveSpeed, as this method may affect it
							if (this.ability[i].activate()) {
								client.sendMessage(new AbilityActivatedMessage(this.getID(), this.ability[i].getID()));
							}
						}
					}
				}
			}

			super.serverAndClientProcess(null, client, tpf_secs, serverTime);

			storeAvatarPosition(serverTime);

			// Position camera at node
			Vector3f vec = this.getWorldTranslation();
			cam.getLocation().x = vec.x;
			cam.getLocation().y = vec.y + avatarModel.getCameraHeight();
			cam.getLocation().z = vec.z;
			cam.update();
		}

		if (hud != null) {
			hud.processByClient((AbstractGameClient)client, tpf_secs);
		}

		if (Globals.SHOW_SERVER_AVATAR_ON_CLIENT) {
			EntityPositionData epd = historicalPositionData.calcPosition(System.currentTimeMillis(), false);
			if (epd != null) {
				debugNode.setLocalTranslation(epd.position);
			}
		}
	}


	public void storeAvatarPosition(long serverTime) {
		Vector3f pos = getWorldTranslation();
		this.clientAvatarPositionData.addPositionData(pos, serverTime);
	}


	// Client Avatars have their own special position calculator
	@Override
	public void calcPosition(long serverTimeToUse, float tpf_secs) {
		if (this.isAlive()) {
			if (Globals.TURN_OFF_CLIENT_POS_ADJ) {
				return;
			}
			Vector3f offset = HistoricalPositionCalculator.calcHistoricalPositionOffset(historicalPositionData, clientAvatarPositionData, serverTimeToUse);
			if (offset != null) {
				float diff = offset.length();
				if (Globals.SHOW_SERVER_CLIENT_AVATAR_DIST) {
					Globals.p("Server and client avatars dist: " + diff);
				}
				if (Float.isNaN(diff) || diff > Globals.MAX_MOVE_DIST) {
					// They're so far out, just move them
					Globals.p("Server and client avatars very far apart, forcing move: " + diff);
					Vector3f pos = historicalPositionData.getMostRecent().position;
					Globals.p("Moving client to " + pos);
					this.setWorldTranslation(pos);
					this.simpleRigidBody.resetForces(); // Prevent from keeping falling
				} else {
					SimpleCharacterControl<PhysicalEntity> simplePlayerControl = (SimpleCharacterControl<PhysicalEntity>)this.simpleRigidBody;
					simplePlayerControl.getAdditionalForce().addLocal(offset.mult(.8f));
				}
			}
		} else {
			super.calcPosition(serverTimeToUse, tpf_secs); // Showing history, so just show where server says we are
		}
	}


	@Override
	public Vector3f getShootDir() {
		return this.cam.getDirection();
	}


	public Camera getCamera() {
		return this.cam;
	}


	@Override
	public void handleKilledOnClientSide(PhysicalEntity killer) {
		Globals.p("You have been killed by " + killer);
		this.setAlive(false);
		this.killer = killer;
	}


	@Override
	public Vector3f getBulletStartPos() {
		AbstractGameClient client = (AbstractGameClient)game;
		Spatial s = client.playersWeaponNode.getChild(0);
		Vector3f pos = s.getWorldTranslation().clone();
		pos.y += 0.1f;
		return pos;
	}


}
