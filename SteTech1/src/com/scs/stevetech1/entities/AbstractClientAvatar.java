package com.scs.stevetech1.entities;

import java.util.Iterator;

import com.jme3.asset.TextureKey;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
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
import com.scs.stevetech1.components.IReloadable;
import com.scs.stevetech1.components.IShowOnHUD;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.netmessages.AbilityActivatedMessage;
import com.scs.stevetech1.netmessages.ClientReloadRequestMessage;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.EntityPositionData;
import com.scs.stevetech1.shared.PositionCalculator;

/**
 * This is the superclass for the client-side players avatar.
 *
 */
public abstract class AbstractClientAvatar extends AbstractAvatar implements IShowOnHUD, IProcessByClient, IKillable {

	public Camera cam;
	public PositionCalculator clientAvatarPositionData; // So we know where we were in the past to compare against where the server says we should have been
	private Spatial debugNode;
	public PhysicalEntity killer;

	private Node container;
	private Vector3f tempAvatarDir = new Vector3f();
	private CollisionResults res = new CollisionResults();

	public AbstractClientAvatar(AbstractGameClient _client, int avatarType, int _playerID, IInputDevice _input, Camera _cam, int eid, 
			float x, float y, float z, byte side, IAvatarModel avatarModel, IAvatarControl _avatarControl) {
		super(_client, avatarType, _playerID, _input, eid, side, avatarModel, _avatarControl);

		clientAvatarPositionData = new PositionCalculator(Globals.HISTORY_DURATION_MILLIS, "AbstractClientAvatar_Player"+_playerID);
		cam = _cam;

		if (Globals.FOLLOW_CAM) {
			// Create model to look good
			Spatial amodel = avatarModel.createAndGetModel();

			// Contain model in a separate node so we can rotate it without losing the models own rotation
			container = new Node();
			container.attachChild(amodel); 
			game.getGameNode().attachChild(container);
		}

		this.setWorldTranslation(new Vector3f(x, y, z));

		if (Globals.SHOW_SERVER_AVATAR_ON_CLIENT) {
			createDebugBox();
		}
	}


	private void createDebugBox() {
		Box box1 = new Box(.5f, .1f, .5f);
		debugNode = new Geometry("DebugBox", box1);
		TextureKey key3 = new TextureKey("Textures/neon1.jpg");
		key3.setGenerateMips(true);
		Texture tex3 = game.getAssetManager().loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);

		Material mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
		mat.setTexture("DiffuseMap", tex3);
		debugNode.setMaterial(mat);

		debugNode.setLocalTranslation(0, box1.yExtent/2, 0); // Origin is at the bottom

		game.getGameNode().attachChild(debugNode);

	}


	@Override
	public void setAlive(boolean a) {
		super.setAlive(a);
		if (a) {
			this.killer = null;
		}
	}


	@Override
	public void remove() {
		super.remove();
		if (Globals.SHOW_SERVER_AVATAR_ON_CLIENT) {
			this.debugNode.removeFromParent();
		}
	}


	@Override
	public void processByClient(IClientApp client, float tpfSecs) {
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
			if (this.input != null) {
				// Check for any abilities/guns being fired
				for (int i=0 ; i<this.ability.length ; i++) {
					if (this.ability[i] != null) {
						if (input.isAbilityPressed(i)) { // Must be before we set the walkDirection & moveSpeed, as this method may affect it
							if (this.ability[i].activate()) {
								client.sendMessage(new AbilityActivatedMessage(this, this.ability[i]));
							}
						}
					}
				}
				// Check for reload
				if (input.isReloadPressed()) {
					if (this.ability[0] instanceof IReloadable) {
						client.sendMessage(new ClientReloadRequestMessage(this.ability[0].getID())); // Auto-reload
					}
				}
			}

			final long serverTime = client.getServerTime();

			super.serverAndClientProcess(null, client, tpfSecs, serverTime);

			storeAvatarPosition(serverTime);

			if (Globals.FOLLOW_CAM) {
				// Set position and direction of avatar model, which doesn't get moved automatically
				this.container.setLocalTranslation(this.getWorldTranslation());
				tempAvatarDir.set(this.cam.getDirection());
				tempAvatarDir.y = 0;
				JMEAngleFunctions.rotateToWorldDirection(this.container, tempAvatarDir);
				this.avatarModel.setAnim(super.currentAnimCode);
			}
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


	/**
	 *  Client Avatars have their own special position calculator
	 */
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
					// They're so far out, just move them to where server thinks they should be
					EntityPositionData epd = historicalPositionData.calcPosition(serverTimeToUse, false);
					Vector3f pos = null;///. 
					if (epd != null) {
						pos = epd.position;
					} else {
						pos = historicalPositionData.getMostRecent().position;
					}
					if (Globals.DEBUG_ADJ_AVATAR_POS) {
						Globals.p("Server and client avatars very far apart, forcing move: " + diff);
						Globals.p("Moving client to " + pos);
					}
					this.setWorldTranslation(pos);
					this.simpleRigidBody.resetForces(); // Prevent from keeping falling
				} else {
					// Push the client avatar towards where the server says they are
					SimpleCharacterControl<PhysicalEntity> simplePlayerControl = (SimpleCharacterControl<PhysicalEntity>)this.simpleRigidBody;
					simplePlayerControl.getAdditionalForce().addLocal(offset.mult(0.4f)); //.8f));
				}
			}
		} else {
			super.calcPosition(serverTimeToUse, tpf_secs); // Showing history, so just show where server says we are
		}
	}


	@Override
	public Vector3f getShootDir() {
		if (!Globals.FOLLOW_CAM) {
			return this.cam.getDirection();
		} else {
			Ray r = new Ray(cam.getLocation(), cam.getDirection()); // todo - don't create each time
			res.clear();
			game.getGameNode().collideWith(r, res);
			Iterator<CollisionResult> it = res.iterator();
			while (it.hasNext()) {
				CollisionResult col = it.next();
				Vector3f hitPos = col.getContactPoint();
				Vector3f dir = hitPos.subtract(this.getBulletStartPos()).normalizeLocal(); // todo - don't create each time
				return dir;
			}
			return this.cam.getDirection();
		}


	}


	public Camera getCamera() {
		return this.cam;
	}


	@Override
	public void handleKilledOnClientSide(PhysicalEntity killer) {
		Globals.p("You have been killed by " + killer);
		this.killer = killer;
	}


	/**
	 * Override bullet position so it looks like it's coming from our gun
	 */
	@Override
	public Vector3f getBulletStartPos() {
		AbstractGameClient client = (AbstractGameClient)game;

		Vector3f pos = null;
		if (client.povWeapon == null) {
			pos = this.getWorldTranslation().clone();
			pos.y += 0.5f;
		} else {
			pos = client.povWeapon.getPOVBulletStartPos_Clone();
		}
		return pos;
	}


	/**
	 * Default to shooting from the centre of the screen.  Override if required.
	 * @return
	 */
	public Vector3f getPOVBulletStartPosition() {
		return this.cam.getLocation();
	}

}
