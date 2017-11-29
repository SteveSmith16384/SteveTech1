package com.scs.simplephysics.tests;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.scs.simplephysics.ICollisionListener;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;

public class Simulation implements ICollisionListener<String> {

	private static final float LOOP_INTERVAL = .0001f;
	private static final int REPORT_INTERVAL = 10;
	private static final float TOTAL_DURATION = 100;
	
	private SimplePhysicsController<String> physicsController;

	public static void main(String args[]) {
		new Simulation();
	}


	public Simulation() {
		physicsController = new SimplePhysicsController<String>(this);

		Sphere sphere = new Sphere(8, 8, .5f);
		Geometry ball_geo = new Geometry("Sphere", sphere);
		ball_geo.setLocalTranslation(0, 10f, 0);
		//this.rootNode.attachChild(ball_geo);

		SimpleRigidBody<String> srb = new SimpleRigidBody<String>(ball_geo, physicsController, "");
		
		float time = 1;
		int prevReport = 0;
		while (time <= TOTAL_DURATION) {
			this.physicsController.update(LOOP_INTERVAL);
			if (time > prevReport) {
				p("Time: " + time + "  Pos: " + ball_geo.getWorldTranslation() + "  GravInc:" + srb.currentGravInc);
				prevReport += REPORT_INTERVAL;
			}
			time += LOOP_INTERVAL;
			/*try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/		
		}
	}


	public static void p(String s) {
		//System.out.println(System.currentTimeMillis() + ": " + s);
		System.out.println(s);
	}


	@Override
	public boolean canCollide(SimpleRigidBody<String> a, SimpleRigidBody<String> b) {
		return true;
	}
	

	@Override
	public void collisionOccurred(SimpleRigidBody<String> a, SimpleRigidBody<String> b, Vector3f point) {
		// Do nothing
	}
	

}
