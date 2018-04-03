package com.scs.simplephysics.tests;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.scs.simplephysics.ICollisionListener;
import com.scs.simplephysics.ISimpleEntity;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;

/*
 * Example for "simulating" physics without actually drawing anything.
 * 
 */
public class SimulateFallingBall implements ICollisionListener<String> {

	private static final float LOOP_INTERVAL_SECS = .001f;
	private static final int REPORT_INTERVAL_SECS = 1;
	private static final float TOTAL_DURATION_SECS = 10;
	
	private SimplePhysicsController<String> physicsController;

	public static void main(String args[]) {
		new SimulateFallingBall();
	}


	private SimulateFallingBall() {
		physicsController = new SimplePhysicsController<String>(this, -1, -1);

		Sphere sphere = new Sphere(8, 8, .5f);
		final Geometry ballGeometry = new Geometry("Sphere", sphere);
		ballGeometry.setLocalTranslation(0, 10f, 0);
		ISimpleEntity<String> entity = new SimpleEntityHelper<String>(ballGeometry);

		SimpleRigidBody<String> srb = new SimpleRigidBody<String>(entity, physicsController, true, "ballGeometry");
		this.physicsController.addSimpleRigidBody(srb);
		
		float time = 1;
		int prevReport = 0;
		while (time <= TOTAL_DURATION_SECS) {
			this.physicsController.update(LOOP_INTERVAL_SECS);
			if (time > prevReport) {
				p("Time: " + time + "  Pos: " + ballGeometry.getWorldTranslation() + "  Gravity offset:" + srb.currentGravInc);
				prevReport += REPORT_INTERVAL_SECS;
			}
			time += LOOP_INTERVAL_SECS;
		}
	}


	public static void p(String s) {
		System.out.println(s);
	}


	@Override
	public boolean canCollide(SimpleRigidBody<String> a, SimpleRigidBody<String> b) {
		return true;
	}
	

	@Override
	public void collisionOccurred(SimpleRigidBody<String> a, SimpleRigidBody<String> b) {
		// Do nothing
	}


}
