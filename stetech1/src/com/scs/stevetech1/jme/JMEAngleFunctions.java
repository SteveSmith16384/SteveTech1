package com.scs.stevetech1.jme;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class JMEAngleFunctions {
	
	public static void turnTowards_Gentle(Spatial spatial, Vector3f target, float pcent) {
		if (pcent <= 0) {
			return; //throw new RuntimeException("Invalid pcent: " + pcent);
		}
		if (pcent > 1) {
			pcent = 1;
		}
		Vector3f dir_to_target = target.subtract(spatial.getWorldTranslation()).normalizeLocal();
		Quaternion target_q = new Quaternion();
		target_q.lookAt(dir_to_target, Vector3f.UNIT_Y);
		Quaternion our_q = spatial.getWorldRotation();
		Quaternion new_q = new Quaternion();
		if (target_q.dot(our_q) > 0.99f) {
			// Just look at it
			new_q = target_q;
		} else {
			new_q.slerp(our_q, target_q, pcent);
		}
		spatial.setLocalRotation(new_q);
	}


	public static void moveForwards(Spatial spatial, float speed) {
		Vector3f forward = spatial.getLocalRotation().mult(Vector3f.UNIT_Z);
		Vector3f offset = forward.mult(speed);
		spatial.move(offset);
	}


	public static Quaternion getRotation(float x, float z) {
		Quaternion target_q = new Quaternion();
		target_q.lookAt(new Vector3f(x, 0, z), Vector3f.UNIT_Y);
		return target_q;
	}


	public static void rotateToDirection(Spatial s, Vector3f dir) {
		Vector3f v = s.getLocalTranslation();
		s.lookAt(v.add(dir), Vector3f.UNIT_Y);
	}


	public static void rotateToDirection(Spatial s, int angdeg) {
		double ang = Math.toRadians(angdeg);
		Vector3f dir = new Vector3f((float)Math.cos(ang), 0, (float)Math.sin(ang));
		rotateToDirection(s, dir);
	}


	public static void rotateBy(Spatial s, int angdeg) {
		double ang = Math.toRadians(angdeg);
		Quaternion q = getRotation((float)Math.cos(ang), (float)Math.sin(ang));
		s.rotate(q);
	}



}
