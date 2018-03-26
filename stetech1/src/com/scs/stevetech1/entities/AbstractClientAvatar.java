package com.scs.stevetech1.entities;

import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.client.HistoricalPositionCalculator;
import com.scs.stevetech1.components.IAvatarModel;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.components.IShowOnHUD;
import com.scs.stevetech1.hud.IHUD;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.EntityPositionData;
import com.scs.stevetech1.shared.PositionCalculator;

public abstract class AbstractClientAvatar extends AbstractAvatar implements IShowOnHUD, IProcessByClient {

	public IHUD hud;
	public Camera cam;
	public PositionCalculator clientAvatarPositionData = new PositionCalculator(true, 500); // So we know where we were in the past to compare against where the server says we should have been
	//private float lastAvatarDiff = 0;
	private Spatial debugNode;	

	public AbstractClientAvatar(AbstractGameClient _client, int _playerID, IInputDevice _input, Camera _cam, IHUD _hud, int eid, 
			float x, float y, float z, int side, IAvatarModel avatarModel, float _moveSpeed, float _jumpSpeed) {
		super(_client, _playerID, _input, eid, side, avatarModel);

		cam = _cam;
		hud = _hud;
		moveSpeed = _moveSpeed;
		this.setJumpForce(_jumpSpeed);

		this.setWorldTranslation(new Vector3f(x, y, z));

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
		//Box box1 = new Box(.5f, .5f, .5f);
		BoundingBox bb = (BoundingBox)super.playerGeometry.getWorldBound();
		//Box box1 = new Box(bb.getXExtent()*2, bb.getYExtent()*2, bb.getZExtent()*2);
		Box box1 = new Box(.5f, .1f, .5f);
		debugNode = new Geometry("DebugBox", box1);
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
		debugNode.setMaterial(floor_mat);

		debugNode.setLocalTranslation(0, box1.yExtent/2, 0); // Origin is at the bottom

		//debugNode = new Node();
		//debugNode.attachChild(geometry);
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

		if (hud != null) {
			hud.processByClient(client, tpf_secs);
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
		if (Globals.ONLY_ADJUST_CLIENT_ON_MOVE && !super.playerWalked) { // Only adjust us if the player tried to move?
			return;
		}
		if (Globals.TURN_OFF_CLIENT_POS_ADJ) {
			return;
		}
		Vector3f offset = HistoricalPositionCalculator.calcHistoricalPositionOffset(serverPositionData, clientAvatarPositionData, serverTimeToUse);
		if (offset != null) {
			float diff = offset.length();
			if (Globals.SHOW_SERVER_CLIENT_AVATAR_DIST) {
				Globals.p("Server and client avatars dist: " + diff);
			}
			//lastAvatarDiff = diff;
			if (Float.isNaN(diff) || diff > Globals.MAX_MOVE_DIST) {
				Globals.p("Server and client avatars very far apart, forcing move: " + diff);
				// They're so far out, just move them
				this.setWorldTranslation(serverPositionData.getMostRecent().position); 
			} else {
				//pe.adjustWorldTranslation(offset);
				adjustWorldTranslation(offset.mult(.5f));

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


	@Override
	protected boolean acceptInput() {
		/*
		// Don't allow client to manually move if too out of sync with server
		boolean b = lastAvatarDiff < 0.1f; // todo - get more scientific distance
		if (!b) {
			Globals.p("Too far away - not accepting input");
		}
		return b;*/
		return true;
	}

}
