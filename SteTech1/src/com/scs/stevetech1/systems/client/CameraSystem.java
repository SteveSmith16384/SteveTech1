package com.scs.stevetech1.systems.client;

import java.util.Iterator;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.server.RayCollisionData;
import com.scs.stevetech1.systems.AbstractSystem;

public class CameraSystem extends AbstractSystem {

	private static final float MAX_FOLLOW = 3f;
	private boolean followCam;

	private AbstractGameClient game;
	//private LinkedList<Vector3f> camPositions;

	public CameraSystem(AbstractGameClient _game, boolean _followCam) {
		game = _game;
		followCam = _followCam;
		if (followCam) {
			//camPositions = new LinkedList<>();
		}
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
			cam.update();

		} else {
			/*Vector3f targetpos = avatar.getWorldTranslation().clone();
			targetpos.y +=  + avatar.avatarModel.getCameraHeight();
			if (camPositions.size() == 0) {
				camPositions.add(targetpos);
			} else {
				// Add to list?
				Vector3f latestPos = this.camPositions.getLast();
				if (latestPos.distance(targetpos) > 0.1f) {
					camPositions.add(targetpos);
				}
			}

			float dist = targetpos.distance(cam.getLocation());
			if (dist > MAX_FOLLOW && this.camPositions.size() > 1) {
				this.camPositions.remove(0);
				cam.setLocation(this.camPositions.getFirst());
				dist = targetpos.distance(cam.getLocation());
			}
			//cam.lookAt(targetpos, Vector3f.UNIT_Y);
			 */

			Ray r = new Ray(avatar.getWorldTranslation(), cam.getDirection());
			r.setLimit(MAX_FOLLOW);
			CollisionResults res = new CollisionResults();
			int c = game.getGameNode().collideWith(r, res);
			boolean found = false;
			if (c == 0) {
				//Vector3f add = cam.getLocation().normalize().mult(MAX_FOLLOW);
				//cam.setLocation(avatar.getWorldTranslation().add(add));
				//Globals.p("No Ray collisions");
			} else {
				//cam.setLocation(res.getClosestCollision().getContactPoint());
				Iterator<CollisionResult> it = res.iterator();
				while (it.hasNext()) {
					CollisionResult col = it.next();
					if (col.getDistance() > r.getLimit()) { // Keep this in! collideWith() seems to ignore it  
						//Vector3f add = cam.getLocation().normalize().mult(MAX_FOLLOW);
						//cam.setLocation(avatar.getWorldTranslation().add(add));
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
							Vector3f add = cam.getDirection().normalize().mult(MAX_FOLLOW);
							cam.setLocation(avatar.getWorldTranslation().add(add));
							found = true;
						}
					}
				}
			}

			if (!found) {
				Vector3f add = cam.getLocation().normalize().mult(MAX_FOLLOW);
				cam.setLocation(avatar.getWorldTranslation().add(add));
			}

			cam.update();
		}
	}

}
