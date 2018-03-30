package com.scs.simplephysics.tests;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.scs.simplephysics.ICollisionListener;
import com.scs.simplephysics.ISimpleEntity;
import com.scs.simplephysics.SimpleCharacterControl;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;

public class SimulateCollidingBoxes implements ICollisionListener<String> {

	private static final float SPEED = 7f;
	private static final int SIZE = 10;
	private static final float LOOP_INTERVAL_SECS = 1f/50; // FPS
	private static final int TOTAL_ITERATIONS = 100;

	private SimplePhysicsController<String> physicsController;
	private SimpleRigidBody<String> box1, box2;

	public static void main(String args[]) {
		new SimulateCollidingBoxes();
	}


	private SimulateCollidingBoxes() {
		physicsController = new SimplePhysicsController<String>(this, SIZE/2, 1);

		box1 = this.createBox(1f);
		box2 = this.createBox(SIZE-1f);
		this.createFloor();

		for (int i=0 ; i<TOTAL_ITERATIONS ; i++) {
			//box1.setAdditionalForce(new Vector3f(0f, 0f, SPEED));
			//box2.setAdditionalForce(new Vector3f(0f, 0f, -SPEED));
			box1.getAdditionalForce().set(new Vector3f(0f, 0f, SPEED));
			box2.getAdditionalForce().set(new Vector3f(0f, 0f, -SPEED));
			this.physicsController.update(LOOP_INTERVAL_SECS);

			p("Iteration: " + i);
			p("Box 1:" + box1.getBoundingBox().getCenter());
			p("Box 2:" + box2.getBoundingBox().getCenter());

		}
	}


	private SimpleRigidBody<String> createBox(float pos) {
		Box box = new Box(.5f, .5f, .5f);
		final Geometry boxGeometry = new Geometry("Box", box);
		boxGeometry.setLocalTranslation(1f, 2f, pos); // origin is the middle
		ISimpleEntity<String> entity = new SimpleEntityHelper<String>(boxGeometry);
		
		SimpleCharacterControl<String> srb = new SimpleCharacterControl<String>(entity, physicsController, "boxGeometry");
		this.physicsController.addSimpleRigidBody(srb);

		return srb;
	}


	private void createFloor() {
		Box floor = new Box(SIZE, 1, SIZE);
		final Geometry floorGeometry = new Geometry("floor", floor);
		floorGeometry.setLocalTranslation(SIZE/2, 0, SIZE/2); // origin is TL.
		ISimpleEntity<String> entity = new SimpleEntityHelper<String>(floorGeometry);
		SimpleRigidBody<String> srb = new SimpleRigidBody<String>(entity, physicsController, false, "floorGeometry");
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
		if (a == box1 || b == box2) {
			if (a == box2 || b == box2) {
				p("Collision between " + a.userObject + " and " + b.userObject);
			}
		}
	}


}
