package com.scs.stevetech1.entities;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Camera.FrustumIntersect;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.simplephysics.SimpleCharacterControl;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.client.HistoricalPositionCalculator;
import com.scs.stevetech1.client.syncposition.AdjustByFractionOfDistance;
import com.scs.stevetech1.client.syncposition.ICorrectClientEntityPosition;
import com.scs.stevetech1.components.IAvatarModel;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.components.IShowOnHUD;
import com.scs.stevetech1.hud.HUD;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.EntityPositionData;
import com.scs.stevetech1.shared.PositionCalculator;

public abstract class AbstractClientAvatar extends AbstractAvatar implements IShowOnHUD, IProcessByClient {

	public HUD hud;
	public Camera cam;
	private ICorrectClientEntityPosition syncPos;
	public PositionCalculator clientAvatarPositionData = new PositionCalculator(true, 500); // So we know where we were in the past to compare against where the server says we should have been

	private Node debugNode;	

	public AbstractClientAvatar(AbstractGameClient _client, int _playerID, IInputDevice _input, Camera _cam, HUD _hud, int eid, 
			float x, float y, float z, int side, IAvatarModel _zm) {
		super(_client, _playerID, _input, eid, side, _zm);

		cam = _cam;
		hud = _hud;

		this.setWorldTranslation(new Vector3f(x, y, z));

		//syncPos = new InstantPositionAdjustment();
		//syncPos = new MoveSlowlyToCorrectPosition();
		//syncPos = new AdjustBasedOnDistance();
		syncPos = new AdjustByFractionOfDistance();
		/*
		SimpleCharacterControl<PhysicalEntity> simplePlayerControl = (SimpleCharacterControl<PhysicalEntity>)this.simpleRigidBody; 
		simplePlayerControl.setJumpForce(jumpForce);
		 */
		_client.currentAvatar = this;

		if (Globals.SHOW_SERVER_AVATAR_ON_CLIENT) {
			createDebugBox();
		}
	}


	private void createDebugBox() {
		Box box1 = new Box(.5f, .5f, .5f);
		Geometry g = new Geometry("DebugBox", box1);
		TextureKey key3 = new TextureKey("Textures/neon1.jpg");
		key3.setGenerateMips(true);
		Texture tex3 = game.getAssetManager().loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);

		Material floor_mat = null;
		if (Globals.LIGHTING) {
			floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
			floor_mat.setTexture("DiffuseMap", tex3);
		} else {
			floor_mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
			floor_mat.setTexture("ColorMap", tex3);
		}

		g.setMaterial(floor_mat);

		g.setLocalTranslation(0, 1f, 0); // Origin is at the bottom

		debugNode = new Node();
		debugNode.attachChild(g);
		game.getGameNode().attachChild(debugNode);

	}


	@Override
	public void processByClient(AbstractGameClient client, float tpf_secs) {
		final long serverTime = client.getServerTime();// System.currentTimeMillis() + client.clientToServerDiffTime;

		if (!this.alive) {
			// Position cam above avatar when they're dead
			Vector3f vec = this.getWorldTranslation();
			cam.getLocation().x = vec.x;
			cam.getLocation().y = 10f;
			cam.getLocation().z = vec.z;

			cam.lookAt(this.getWorldTranslation(), Vector3f.UNIT_Y);
			cam.update();

		} else {
			super.serverAndClientProcess(null, client, tpf_secs, serverTime);

			storeAvatarPosition(serverTime);

			// Position camera at node
			Vector3f vec = this.getWorldTranslation();
			cam.getLocation().x = vec.x;
			cam.getLocation().y = vec.y + avatarModel.getCameraHeight();
			cam.getLocation().z = vec.z;
			cam.update();
		}

		hud.processByClient(client, tpf_secs);

		// These must be after we might use them, so the hud is correct
		if (ability[0] != null) {
			this.hud.setAbilityGunText(this.ability[0].getHudText());
		}
		if (this.ability[1] != null) {
			this.hud.setAbilityOtherText(this.ability[1].getHudText());
		}

		if (Globals.SHOW_SERVER_AVATAR_ON_CLIENT) {
			//long serverTimePast = serverTime - Globals.CLIENT_RENDER_DELAY; // Render from history
			EntityPositionData epd = serverPositionData.calcPosition(System.currentTimeMillis(), false);
			if (epd != null) {
				debugNode.setLocalTranslation(epd.position);
			}
		}
	}


	public void storeAvatarPosition(long serverTime) {
		Vector3f pos = getWorldTranslation();
		this.clientAvatarPositionData.addPositionData(pos, null, serverTime);
	}


	// Client Avatars have their own special position calculator
	@Override
	public void calcPosition(AbstractGameClient mainApp, long serverTimeToUse, float tpf_secs) {
		if (Globals.ONLY_ADJUST_CLIENT_ON_MOVE == false || super.playerWalked) { // Only adjust us if the player tried to move?
			Vector3f offset = HistoricalPositionCalculator.calcHistoricalPositionOffset(serverPositionData, clientAvatarPositionData, serverTimeToUse);
			if (offset != null) {
				float diff = offset.length();
				if (Float.isNaN(diff) || diff > Globals.MAX_MOVE_DIST) {
					Globals.p("Far out, man! " + diff);
					// They're so far out, just move them
					this.setWorldTranslation(serverPositionData.getMostRecent().position); 
				} else {
					this.syncPos.adjustPosition(this, offset, tpf_secs);
				}
			}
		}
	}


	@Override
	public Vector3f getShootDir() {
		return this.cam.getDirection();
	}


	public Camera getCamera() {
		return this.cam;
	}


}
