package com.scs.stetech1.entities;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Camera.FrustumIntersect;
import com.scs.stetech1.client.AbstractGameClient;
import com.scs.stetech1.client.ClientAvatarPositionCalc;
import com.scs.stetech1.client.syncposition.ICorrectClientEntityPosition;
import com.scs.stetech1.client.syncposition.InstantPositionAdjustment;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.components.IProcessByClient;
import com.scs.stetech1.components.IShowOnHUD;
import com.scs.stetech1.hud.HUD;
import com.scs.stetech1.input.IInputDevice;
import com.scs.stetech1.netmessages.AbilityUpdateMessage;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityPositionData;
import com.scs.stetech1.shared.IAbility;
import com.scs.stetech1.shared.PositionCalculator;

public abstract class ClientPlayersAvatar extends AbstractAvatar implements IShowOnHUD, IProcessByClient {

	public HUD hud;
	public Camera cam;
	private ICorrectClientEntityPosition syncPos;
	public PositionCalculator clientAvatarPositionData = new PositionCalculator(true, 500); // So we know where we were in the past to compare against where the server says we should have been

	public ClientPlayersAvatar(AbstractGameClient _module, int _playerID, IInputDevice _input, Camera _cam, HUD _hud, int eid, float x, float y, float z, int side) {
		super(_module, _playerID, _input, eid, side);

		cam = _cam;
		hud = _hud;

		this.setWorldTranslation(new Vector3f(x, y, z));

		syncPos = new InstantPositionAdjustment();
		//syncPos = new MoveSlowlyToCorrectPosition();
		//syncPos = new AdjustBasedOnDistance();

		this.simpleRigidBody.setGravity(0); // scs todo?

	}


	@Override
	public void process(AbstractGameClient client, float tpf) {
		final long serverTime = System.currentTimeMillis() + client.clientToServerDiffTime;

		storeAvatarPosition(serverTime); // todo - don't have sep list, use serverPositionData client-side

		super.serverAndClientProcess(null, client, tpf);

		hud.process(client, tpf);

		// Position camera at node
		Vector3f vec = this.getWorldTranslation();
		cam.getLocation().x = vec.x;
		cam.getLocation().y = vec.y + PLAYER_HEIGHT;
		cam.getLocation().z = vec.z;
		cam.update();

		// Rotate us to point in the direction of the camera
		Vector3f lookAtPoint = cam.getLocation().add(cam.getDirection().mult(10));
		lookAtPoint.y = cam.getLocation().y; // Look horizontal
		//todo -readd? But rotating spatial makes us stick to the floor   this.playerGeometry.lookAt(lookAtPoint, Vector3f.UNIT_Y);

		// Move cam fwd so we don't see ourselves
		//cam.setLocation(cam.getLocation().add(cam.getDirection().mult(PLAYER_RAD)));
		//cam.update();

	}


	public void storeAvatarPosition(long serverTime) {
		// Store our position
		EntityPositionData epd = new EntityPositionData();
		epd.serverTimestamp = serverTime;
		epd.position = getWorldTranslation().clone();
		//epd.rotation not required
		this.clientAvatarPositionData.addPositionData(epd);

	}


	// Avatars have their own special position calculator
	@Override
	public void calcPosition(AbstractGameClient mainApp, long serverTimeToUse) {
		if (Settings.SYNC_CLIENT_POS) {
			Vector3f offset = ClientAvatarPositionCalc.calcHistoricalPositionOffset(serverPositionData, clientAvatarPositionData, serverTimeToUse, mainApp.pingRTT/2);
			if (offset != null) {
				this.syncPos.adjustPosition(this, offset);
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
	public void process(AbstractGameServer server, float tpf_secs) {
		// Do nothing

	}


	public void updateAbility(AbilityUpdateMessage aum) {
		IAbility a = null;
		if (aum.abilityNum == 0) {
			a = this.abilityGun;
		} else if (aum.abilityNum == 1) {
			a = this.abilityOther;
		} else {
			throw new RuntimeException("Unknown ability: " + aum.abilityNum);
		}
		a.decode(aum);
	}
	
}
