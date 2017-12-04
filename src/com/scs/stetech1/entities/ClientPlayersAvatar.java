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
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityPositionData;
import com.scs.stetech1.shared.PositionCalculator;

public abstract class ClientPlayersAvatar extends AbstractPlayersAvatar implements IShowOnHUD, IProcessByClient {

	public HUD hud;
	public Camera cam;
	private ICorrectClientEntityPosition syncPos;
	public PositionCalculator clientAvatarPositionData = new PositionCalculator(true, 500);

	public ClientPlayersAvatar(AbstractGameClient _module, int _playerID, IInputDevice _input, Camera _cam, HUD _hud, int eid, float x, float y, float z, int side) {
		super(_module, _playerID, _input, eid, side);

		cam = _cam;
		hud = _hud;

		this.setWorldTranslation(new Vector3f(x, y, z));

		syncPos = new InstantPositionAdjustment();
		//syncPos = new MoveSlowlyToCorrectPosition();
		//syncPos = new AdjustBasedOnDistance();

		this.simpleRigidBody.setGravity(0); // scs todo

	}


	@Override
	public void process(AbstractGameClient client, float tpf) {
		final long serverTime = System.currentTimeMillis() + client.clientToServerDiffTime;
		
		storeAvatarPosition(serverTime);

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
		this.playerGeometry.lookAt(lookAtPoint, Vector3f.UNIT_Y);
		//this.getMainNode().lookAt(lookAtPoint.clone(), Vector3f.UNIT_Y);  This won't rotate the model since it's locked to the physics controller

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


	/*public Vector3f getPointOnFloor(float range) {
		Vector3f from = this.cam.getLocation();
		Vector3f to = this.cam.getDirection().normalize().multLocal(range).addLocal(from);
		List<PhysicsRayTestResult> results = game.getBulletAppState().getPhysicsSpace().rayTest(from, to);
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
	}*/


	public FrustumIntersect getInsideOutside(PhysicalEntity entity) {
		FrustumIntersect insideoutside = cam.contains(entity.getMainNode().getWorldBound());
		return insideoutside;
	}


	@Override
	public void process(AbstractGameServer server, float tpf_secs) {
		// Do nothing

	}


}
