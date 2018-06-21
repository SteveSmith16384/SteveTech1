package com.scs.stevetech1.jme;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import ssmith.lang.NumberFunctions;

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


	public static float getAngleBetween(Spatial spatial, Vector3f target) {
		Vector3f dir_to_target = target.subtract(spatial.getWorldTranslation()).normalizeLocal();
		Vector3f forward = spatial.getLocalRotation().mult(Vector3f.UNIT_Z).normalizeLocal();
		//float diff = forward.distance(dir_to_target);
		//return diff;
		return dir_to_target.angleBetween(forward);
	}


	public static Quaternion getRotation(float x, float z) {
		Quaternion target_q = new Quaternion();
		target_q.lookAt(new Vector3f(x, 0, z), Vector3f.UNIT_Y);
		return target_q;
	}


	public static void rotateToDirection(Spatial s, Vector3f dir) {
		Vector3f v = s.getLocalTranslation();
		s.lookAt(v.add(dir), Vector3f.UNIT_Y);
		//s.lookAt(dir.addLocal(v), Vector3f.UNIT_Y); // scs new
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


	public static Vector3f getRandomDirection_4() {
		int i = NumberFunctions.rnd(0, 3);
		switch (i) {
		case 0: return new Vector3f(1f, 0, 0);
		case 1: return new Vector3f(-1f, 0, 0);
		case 2: return new Vector3f(0f, 0, 1f);
		case 3: return new Vector3f(0f, 0, -1f);
		}
		throw new RuntimeException("Invalid direction: " + i);
	}


	public static Vector3f getRandomDirection_8() {
		int i = NumberFunctions.rnd(0,  7);
		switch (i) {
		case 0: return new Vector3f(1f, 0, 0);
		case 1: return new Vector3f(-1f, 0, 0);
		case 2: return new Vector3f(0f, 0, 1f);
		case 3: return new Vector3f(0f, 0, -1f);

		// Diagonals
		case 4: return new Vector3f(1f, 0, 1f);
		case 5: return new Vector3f(-1f, 0, -1f);
		case 6: return new Vector3f(-1f, 0, 1f);
		case 7: return new Vector3f(1f, 0, -1f);
		}
		throw new RuntimeException("Invalid direction: " + i);
	}



}
