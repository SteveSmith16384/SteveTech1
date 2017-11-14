package com.scs.stetech1.client.entities;

import java.util.List;

import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Camera.FrustumIntersect;
import com.scs.stetech1.client.ClientAvatarPositionCalc;
import com.scs.stetech1.client.GenericClient;
import com.scs.stetech1.client.syncposition.AdjustBasedOnDistance;
import com.scs.stetech1.client.syncposition.ICorrectClientEntityPosition;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.components.IShowOnHUD;
import com.scs.stetech1.hud.HUD;
import com.scs.stetech1.input.IInputDevice;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.AbstractPlayersAvatar;
import com.scs.stetech1.shared.entities.Entity;
import com.scs.stetech1.shared.entities.PhysicalEntity;

public class ClientPlayersAvatar extends AbstractPlayersAvatar implements IShowOnHUD {

	public HUD hud;
	public Camera cam;
	private GenericClient game;
	private ICorrectClientEntityPosition syncPos;

	public ClientPlayersAvatar(GenericClient _module, int _playerID, IInputDevice _input, Camera _cam, HUD _hud, int eid, float x, float y, float z) {
		super(_module, _playerID, _input, eid);

		game = _module;
		cam = _cam;
		hud = _hud;

		this.setWorldTranslation(new Vector3f(x, y, z));

		//syncPos = new InstantPositionAdjustment(); Problems
		//syncPos = new MoveSlowlyToCorrectPosition(0.1f);
		syncPos = new AdjustBasedOnDistance();
	}


	@Override
	public void process(float tpf) {
		super.process(tpf);

		hud.process(tpf);

		// Position camera at node
		Vector3f vec = this.getWorldTranslation();// getMainNode().getWorldTranslation();
		// cam.setLocation(new Vector3f(vec.x, vec.y + (PLAYER_HEIGHT/2), vec.z));
		// Avoid creating new Vector3f
		cam.getLocation().x = vec.x;
		cam.getLocation().y = vec.y + (PLAYER_HEIGHT/2);
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


	@Override
	public void calcPosition(GenericClient mainApp, long serverTimeToUse) {
		Vector3f offset = ClientAvatarPositionCalc.calcHistoricalPositionOffset(serverPositionData, game.clientAvatarPositionData, serverTimeToUse, mainApp.pingRTT/2);
		if (offset != null) {
			//if (diff > Settings.MAX_CLIENT_POSITION_DISCREP) {
			this.syncPos.adjustPosition(this, offset);
			//}
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


	public Vector3f getPointOnFloor(float range) {
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
	}


	public FrustumIntersect getInsideOutside(PhysicalEntity entity) {
		FrustumIntersect insideoutside = cam.contains(entity.getMainNode().getWorldBound());
		return insideoutside;
	}


}
