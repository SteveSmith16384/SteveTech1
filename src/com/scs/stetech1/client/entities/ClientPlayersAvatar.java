package com.scs.stetech1.client.entities;

import java.util.List;

import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Camera.FrustumIntersect;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.components.IShowOnHUD;
import com.scs.stetech1.hud.HUD;
import com.scs.stetech1.input.IInputDevice;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.AbstractPlayersAvatar;
import com.scs.stetech1.shared.IEntityController;

public class ClientPlayersAvatar extends AbstractPlayersAvatar implements IShowOnHUD {

	//private static final Vector3f CAM_HEIGHT_OFFSET = new Vector3f(0, PLAYER_HEIGHT/2, 0);
	public HUD hud;
	public Camera cam;

	public ClientPlayersAvatar(IEntityController _module, int _playerID, IInputDevice _input, Camera _cam, HUD _hud, float x, float y, float z) {
		super(_module, _playerID, _input);
		
		cam = _cam;
		hud = _hud;
		
		this.setWorldTranslation(new Vector3f(x, y, z));

	}
	
	
	@Override
	public void process(float tpf) {
		super.process(tpf);
		
		hud.process(tpf);

		// Position camera at node
		Vector3f vec = getMainNode().getWorldTranslation();
		//cam.setLocation(new Vector3f(vec.x, vec.y + (PLAYER_HEIGHT/2), vec.z));
		cam.getLocation().x = vec.x;
		cam.getLocation().y = vec.y + (PLAYER_HEIGHT/2);
		cam.getLocation().z = vec.z;

		// Rotate us to point in the direction of the camera
		Vector3f lookAtPoint = cam.getLocation().add(cam.getDirection().mult(10));
		//gun.lookAt(lookAtPoint.clone(), Vector3f.UNIT_Y);
		lookAtPoint.y = cam.getLocation().y; // Look horizontal
		this.playerGeometry.lookAt(lookAtPoint, Vector3f.UNIT_Y);
		//this.getMainNode().lookAt(lookAtPoint.clone(), Vector3f.UNIT_Y);  This won't rotate the model since it's locked to the physics controller

		// Move cam fwd so we don't see ourselves
		//cam.setLocation(cam.getLocation().add(cam.getDirection().mult(PLAYER_RAD)));
		//cam.update();

	}
	
	
	@Override
	public void hasSuccessfullyHit(IEntity e) {
		// Do nothing - done server-side
	}
	
	
	@Override
	public Vector3f getWorldTranslation() {
		return this.cam.getLocation();
		//return playerControl.getPhysicsRigidBody().getPhysicsLocation();  This is very low to the ground!
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




}
