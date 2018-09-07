package com.scs.stevetech1.jme;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import ssmith.lang.NumberFunctions;

/**
 * Static helper functions for JME.
 * @author stephencs
 *
 */
public class JMEAngleFunctions {

	private JMEAngleFunctions() {
		
	}
	
	
	public static void turnTowards_Gentle(Spatial spatial, Vector3f target, float frac) {
		if (frac <= 0) {
			return; //throw new RuntimeException("Invalid pcent: " + pcent);
		}
		if (frac > 1) {
			frac = 1;
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
			new_q.slerp(our_q, target_q, frac);
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


	public static Quaternion getYAxisRotation(float x, float z) {
		Quaternion target_q = new Quaternion();
		target_q.lookAt(new Vector3f(x, 0, z), Vector3f.UNIT_Y);
		return target_q;
	}


	public static void rotateToWorldDirection(Spatial s, Vector3f dir) {
		Vector3f v = s.getLocalTranslation();
		s.lookAt(v.add(dir), Vector3f.UNIT_Y);
		//s.lookAt(dir.addLocal(v), Vector3f.UNIT_Y); // scs new
	}


	public static void rotateToWorldDirectionYAxis(Spatial s, int angdeg) {
		double ang = Math.toRadians(angdeg);
		Vector3f dir = new Vector3f((float)Math.cos(ang), 0, (float)Math.sin(ang));
		rotateToWorldDirection(s, dir);
	}


	public static void rotateYAxisBy(Spatial s, int angdeg) {
		double ang = Math.toRadians(angdeg);
		Quaternion q = getYAxisRotation((float)Math.cos(ang), (float)Math.sin(ang));
		s.rotate(q);
	}


	public static Vector3f getRandomDirection_All() {
		float x = NumberFunctions.rndFloat(-1, 1);
		float z = NumberFunctions.rndFloat(-1, 1);
		return new Vector3f(x, 0, z).normalizeLocal();
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


	// This function hasn't been tested
	public static void turnLeft(Spatial spatial){
		//Vector3f direction = new Vector3f(0, 0, 1);
		Quaternion rotateLeft = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y);
		spatial.rotate(rotateLeft);
		//rotateLeft.mult(direction, direction);
		//system
		//System.out.println("direction: " + direction);
	}


	// This function hasn't been tested
	public static void turnUp(Spatial spatial){
		//Vector3f direction = new Vector3f(0, 0, 1);
		Quaternion rotateLeft = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X);
		spatial.rotate(rotateLeft);
		//rotateLeft.mult(direction, direction);
		//system
		//System.out.println("direction: " + direction);
	}


	// This function hasn't been tested
	public static void Turn(Spatial spatial, float timeFactor) {
		Quaternion q1 = new Quaternion(); //start
		Quaternion q2 = new Quaternion();
		q2.fromAngles(new float[] {-90,0,0});//end

		//during update rotate a little bit based on time
		spatial.setLocalRotation(q1.slerp(q1,q2,timeFactor));
		
		//spatial.ro
	}
	
	
	public static Vector3f turnRight(Vector3f v) {
		Vector3f v2 = v.normalize();
		//float z = v.z;
		v2.x += 1;
		v2.y = v.y;
		v2.z = v.z - 1;
		
		if (v2.x > 1) {
			v2.x = 0;
			v2.z -= 1;
		} else if (v2.z < -1) {
			v2.x -= 1;
			v2.z = 1;
		}
		
		return v2;
	}
}
