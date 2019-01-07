package com.scs.stevetech1.systems.client;

import java.util.Iterator;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.systems.AbstractSystem;

public class CameraSystem extends AbstractSystem {

	private boolean followCam;
	private float followDist = 1f;
	private float shoulderAngle = 0f;
	private AbstractGameClient game;

	public CameraSystem(AbstractGameClient _game, boolean _followCam) {
		game = _game;
		followCam = _followCam;
	}


	public void setupFollowCam(float dist, float angle) {
		this.followDist = dist;
		shoulderAngle = angle;
	}


	public void process(Camera cam, AbstractClientAvatar avatar) {
		if (avatar == null || cam == null || !avatar.isAlive()) {
			return;
		}

		if (!followCam) {
			// Position camera at node
			Vector3f vec = avatar.getWorldTranslation();
			cam.getLocation().x = vec.x;
			cam.getLocation().y = vec.y + avatar.avatarModel.getCameraHeight();
			cam.getLocation().z = vec.z;
/*
			if (Globals.DEBUG_3D_PROBLEM) {
				cam.getLocation().y = 20;
			}
			*/
			cam.update();

		} else {
			Vector3f avatarPos = avatar.getWorldTranslation().clone(); // todo - don't create each time
			avatarPos.y += avatar.avatarModel.getCameraHeight() + .1f;

			Vector3f dir = cam.getDirection().mult(-1);
			if (shoulderAngle != 0) {
				Quaternion rotQ = new Quaternion();
				rotQ.fromAngleAxis(shoulderAngle, Vector3f.UNIT_Y);
				rotQ.multLocal(dir).normalizeLocal();
			}
			Ray r = new Ray(avatarPos, dir);
			r.setLimit(followDist);
			CollisionResults res = new CollisionResults();
			int c = game.getGameNode().collideWith(r, res);
			boolean found = false;
			if (c > 0) {
				Iterator<CollisionResult> it = res.iterator();
				while (it.hasNext()) {
					CollisionResult col = it.next();
					if (col.getDistance() > r.getLimit()) { // Keep this in! collideWith() seems to ignore it.
						break;
					}
					Spatial s = col.getGeometry();
					while (s.getUserData(Globals.ENTITY) == null) {
						s = s.getParent();
						if (s == null) {
							break;
						}
					}
					if (s != null && s.getUserData(Globals.ENTITY) != null) {
						PhysicalEntity pe = (PhysicalEntity)s.getUserData(Globals.ENTITY);
						if (pe != avatar) {
							float dist = col.getDistance();
							if (dist > 0.1f) {
								dist -= 0.1f;
							}
							Vector3f add = dir.multLocal(dist);
							cam.setLocation(avatarPos.add(add));
							found = true;
							break;
						}
					}
				}
			}

			if (!found) {
				Vector3f add = dir.multLocal(followDist);
				cam.setLocation(avatarPos.add(add));
			}

			cam.update();
		}
	}

}
