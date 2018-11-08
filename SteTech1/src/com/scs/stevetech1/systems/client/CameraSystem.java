package com.scs.stevetech1.systems.client;

import java.util.LinkedList;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.systems.AbstractSystem;

public class CameraSystem extends AbstractSystem {

	private static final float MAX_FOLLOW = 3f;
	private boolean followCam;

	private LinkedList<Vector3f> camPositions;

	public CameraSystem(boolean _followCam) {
		followCam = _followCam;
		if (followCam) {
			camPositions = new LinkedList<>();
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
			Vector3f targetpos = avatar.getWorldTranslation().clone();
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
			cam.update();
		}
	}

}
