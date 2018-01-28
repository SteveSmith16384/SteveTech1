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
import com.scs.stevetech1.client.syncposition.ICorrectClientEntityPosition;
import com.scs.stevetech1.client.syncposition.InstantPositionAdjustment;
import com.scs.stevetech1.components.IAnimatedAvatarModel;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IPreprocess;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.components.IShowOnHUD;
import com.scs.stevetech1.hud.HUD;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.EntityPositionData;
import com.scs.stevetech1.shared.PositionCalculator;

public abstract class AbstractClientAvatar extends AbstractAvatar implements IShowOnHUD, IProcessByClient {

	public HUD hud;
	public Camera cam;
	private ICorrectClientEntityPosition syncPos;
	public PositionCalculator clientAvatarPositionData = new PositionCalculator(true, 500); // So we know where we were in the past to compare against where the server says we should have been

	private Node debugNode;

	public AbstractClientAvatar(AbstractGameClient _module, int _playerID, IInputDevice _input, Camera _cam, HUD _hud, int eid, 
			float x, float y, float z, int side, IAnimatedAvatarModel _zm) { //, IGetAvatarAnimationString animCodes, float _camHeight) {
		super(_module, _playerID, _input, eid, side, _zm);

		cam = _cam;
		hud = _hud;

		this.setWorldTranslation(new Vector3f(x, y, z));

		syncPos = new InstantPositionAdjustment();
		//syncPos = new MoveSlowlyToCorrectPosition();
		//syncPos = new AdjustBasedOnDistance();

		this.simpleRigidBody.setGravity(0);

		SimpleCharacterControl<PhysicalEntity> simplePlayerControl = (SimpleCharacterControl<PhysicalEntity>)this.simpleRigidBody; 
		simplePlayerControl.setJumpForce(Globals.JUMP_FORCE/4); // Different to server side, since we don't have gravity!

		_module.currentAvatar = this;

		if (Globals.SHOW_SERVER_AVATAR_ON_CLIENT) {
			createDebugBox();
		}
	}


	private void createDebugBox() {
		Box box1 = new Box(.5f, .5f, .5f); // todo - use IAnimatedAvatarModel?
		Geometry g = new Geometry("Crate", box1);
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
		game.getRootNode().attachChild(debugNode);

	}


	@Override
	public void processByClient(AbstractGameClient client, float tpf_secs) {
		final long serverTime = client.getServerTime();// System.currentTimeMillis() + client.clientToServerDiffTime;

		if (!this.alive) {
			// Position cam above avatar
			Vector3f vec = this.getWorldTranslation();
			cam.getLocation().x = vec.x;
			cam.getLocation().y = 10f;
			cam.getLocation().z = vec.z;

			cam.lookAt(this.getWorldTranslation(), Vector3f.UNIT_Y);
			cam.update();
			return;
		}

		Vector3f pos = this.getWorldTranslation().clone(); // todo - remove

		super.serverAndClientProcess(null, client, tpf_secs, serverTime);

		if (Globals.DEBUG_ADJ_AVATAR_POS) {
			Vector3f newPos = this.getWorldTranslation().clone();
			Vector3f diff = newPos.subtract(pos);
			if (diff.length() > 0) {
				Globals.p("Moved client by " + diff);
			} else {
				Globals.p("Client not moved");
			}
			Globals.p("Client position is " + this.getWorldTranslation());
		}

		storeAvatarPosition(serverTime);

		hud.processByClient(client, tpf_secs);

		// Position camera at node
		Vector3f vec = this.getWorldTranslation();
		cam.getLocation().x = vec.x;
		cam.getLocation().y = vec.y + avatarModel.getCameraHeight();
		cam.getLocation().z = vec.z;
		cam.update();

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
		//Globals.p("Storing pos " + pos);
		this.clientAvatarPositionData.addPositionData(pos, null, serverTime);
	}


	// Avatars have their own special position calculator
	@Override
	public void calcPosition(AbstractGameClient mainApp, long serverTimeToUse, float tpf_secs) {
		if (Globals.SYNC_AVATAR_POS) {
			Vector3f offset = HistoricalPositionCalculator.calcHistoricalPositionOffset(serverPositionData, clientAvatarPositionData, serverTimeToUse, mainApp.pingRTT/2);
			if (offset != null) {
				this.syncPos.adjustPosition(this, offset, tpf_secs);
			}
		}
	}


	@Override
	public void hasSuccessfullyHit(IEntity e) {
		// Do nothing - done server-side
	}


	@Override
	public Vector3f getShootDir() {
		return this.cam.getDirection();
	}


	public Camera getCamera() {
		return this.cam;
	}


	public FrustumIntersect getInsideOutside(PhysicalEntity entity) {
		FrustumIntersect insideoutside = cam.contains(entity.getMainNode().getWorldBound());
		return insideoutside;
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		// Do nothing

	}


}
