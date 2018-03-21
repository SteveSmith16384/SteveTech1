package com.scs.simplephysics.tests;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.scs.simplephysics.ICollisionListener;
import com.scs.simplephysics.ISimpleEntity;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;

public class BouncingSimulation implements ICollisionListener<String> {

	private static final float LOOP_INTERVAL_SECS = .001f;
	private static final float REPORT_INTERVAL_SECS = 0.1f;
	private static final float TOTAL_DURATION_SECS = 5;

	private SimplePhysicsController<String> physicsController;
	private Geometry ballGeometry;

	public static void main(String args[]) {
		new BouncingSimulation();
	}


	private BouncingSimulation() {
		physicsController = new SimplePhysicsController<String>(this);

		this.createBall();
		this.createFloor();

		float time = 1;
		int prevReport = 0;
		while (time <= TOTAL_DURATION_SECS) {
			this.physicsController.update(LOOP_INTERVAL_SECS);
			if (time > prevReport) {
				p("Time: " + time + "  Ball Pos: " + ballGeometry.getWorldTranslation());// + "  Gravity offset:" + srb.currentGravInc);
				prevReport += REPORT_INTERVAL_SECS;
			}
			time += LOOP_INTERVAL_SECS;
		}
	}


	private void createBall() {
		Sphere sphere = new Sphere(8, 8, .5f);
		ballGeometry = new Geometry("Sphere", sphere);
		ballGeometry.setLocalTranslation(0, 10f, 0);
		ISimpleEntity<String> entity = new ISimpleEntity<String>() {

			@Override
			public Spatial getSpatial() {
				return ballGeometry;
			}

			@Override
			public void hasMoved() {
				// Do nothing
			}

		};
		SimpleRigidBody<String> srb = new SimpleRigidBody<String>(entity, physicsController, true, "ballGeometry");
		this.physicsController.addSimpleRigidBody(srb);

	}


	private void createFloor() {
		Box box = new Box(10, 1, 10);
		Geometry boxGeometry = new Geometry("box", box);
		boxGeometry.setLocalTranslation(0, 0, 0);
		ISimpleEntity<String> entity = new ISimpleEntity<String>() {

			@Override
			public Spatial getSpatial() {
				return boxGeometry;
			}

			@Override
			public void hasMoved() {
				// Do nothing
			}

		};
		SimpleRigidBody<String> srb = new SimpleRigidBody<String>(entity, physicsController, false, "boxGeometry");
		this.physicsController.addSimpleRigidBody(srb);

	}


	public static void p(String s) {
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
