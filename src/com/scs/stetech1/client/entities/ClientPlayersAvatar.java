package com.scs.stetech1.client.entities;

import java.util.List;

import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Camera.FrustumIntersect;
import com.scs.stetech1.client.ClientAvatarPositionCalc;
import com.scs.stetech1.client.SorcerersClient;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.components.IShowOnHUD;
import com.scs.stetech1.hud.HUD;
import com.scs.stetech1.input.IInputDevice;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.AbstractPlayersAvatar;
import com.scs.stetech1.shared.EntityPositionData;
import com.scs.stetech1.shared.entities.Entity;
import com.scs.stetech1.shared.entities.PhysicalEntity;

public class ClientPlayersAvatar extends AbstractPlayersAvatar implements IShowOnHUD {

	public HUD hud;
	public Camera cam;
	private SorcerersClient game;

	public ClientPlayersAvatar(SorcerersClient _module, int _playerID, IInputDevice _input, Camera _cam, HUD _hud, int eid, float x, float y, float z) {
		super(_module, _playerID, _input, eid);

		game = _module;
		cam = _cam;
		hud = _hud;

		this.setWorldTranslation(new Vector3f(x, y, z));
		//this.getMainNode().setLocalTranslation(x, y, z);

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
	public void calcPosition(SorcerersClient mainApp, long serverTimeToUse) {
		Vector3f offset = ClientAvatarPositionCalc.calcHistoricalPositionOffset(serverPositionData, game.clientAvatarPositionData, serverTimeToUse, mainApp.pingRTT/2);
		if (offset != null) {
			float diff = offset.length();
			if (diff > 0.1f) {
				//Settings.p("Adjusting client by: " + diff);

				// OPTION 1: Get diff between player pos X millis ago and current pos, and re-add this to server pos
				/*Vector3f clientMovementSinceRenderDelay = currentClientAvatarPosition.subtract(clientEPD.position);
				//clientMovementSinceRenderDelay.y = 0; // Don't adjust y-axis
				Vector3f newPos = serverEPD.position.add(clientMovementSinceRenderDelay);*/

				// OPTION 2: Adjust player by halfway between server pos and client pos
				/*Vector3f newPos = new Vector3f();
				newPos.interpolate(serverEPD.position, clientEPD.position, .5f); 
				Settings.p("Moving player to " + newPos);*/

				// OPTION 3: Move player slowly towards server position
				float MAX_MOVE = 0.01f;
				if (diff > MAX_MOVE) {
					offset.normalizeLocal().multLocal(MAX_MOVE);
				} else {
					int zzz = 6;
				}
				Vector3f newPos = mainApp.avatar.getWorldTranslation().add(offset);
				
				this.setWorldTranslation(newPos);
			}
		}

	}


	@Override
	public void hasSuccessfullyHit(IEntity e) {
		// Do nothing - done server-side
	}


	// Don't use cam position, just use main node position
	/*@Override
	public Vector3f getWorldTranslation() {
		return this.cam.getLocation();
		//return playerControl.getPhysicsRigidBody().getPhysicsLocation();  This is very low to the ground!
	}*/


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


	public FrustumIntersect getInsideOutside(PhysicalEntity entity) {
		FrustumIntersect insideoutside = cam.contains(entity.getMainNode().getWorldBound());
		return insideoutside;
	}


	@Override
	public void shoot() {
		// TODO Auto-generated method stub
		
	}




}
