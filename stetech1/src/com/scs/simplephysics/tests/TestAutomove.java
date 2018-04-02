package com.scs.simplephysics.tests;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.scs.simplephysics.ICollisionListener;
import com.scs.simplephysics.ISimpleEntity;
import com.scs.simplephysics.SimpleCharacterControl;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;

public class TestAutomove implements ICollisionListener<String> {

	private static final float LOOP_INTERVAL_SECS = .001f;
	private static final int REPORT_INTERVAL_SECS = 1;
	private static final float TOTAL_DURATION_SECS = 10;
	
	private SimplePhysicsController<String> physicsController;

	public static void main(String args[]) {
		new TestAutomove();
	}


	private TestAutomove() {
		physicsController = new SimplePhysicsController<String>(this, -1, -1, 0, 0.99f);

		SimpleRigidBody<String> box1 = this.createBox("box1", new Vector3f(0, 0, 0));
		SimpleRigidBody<String> box2 = this.createBox("box1", new Vector3f(.5f, 0, 0));
		SimpleRigidBody<String> box3 = this.createBox("box1", new Vector3f(1f, 0, 0));
		
		float time = 1;
		int prevReport = 0;
		while (time <= TOTAL_DURATION_SECS) {
			this.physicsController.update(LOOP_INTERVAL_SECS);
			if (time > prevReport) {
				//p("Time: " + time + "  Pos: " + box1.simpleEntity.getBoundingBox().ce.getWorldTranslation() + "  Gravity offset:" + srb.currentGravInc);
				prevReport += REPORT_INTERVAL_SECS;
			}
			time += LOOP_INTERVAL_SECS;
		}
		
		p("Box 1: " + box1.getBoundingBox().getCenter());
		p("Box 2: " + box2.getBoundingBox().getCenter());
		p("Box 3: " + box3.getBoundingBox().getCenter());
	}


	private SimpleRigidBody<String> createBox(String name, Vector3f pos) {
		Box box = new Box(.5f, .5f, .5f);
		final Geometry boxGeometry = new Geometry(name, box);
		boxGeometry.setLocalTranslation(pos); // origin is the middle
		ISimpleEntity<String> entity = new SimpleEntityHelper<String>(boxGeometry);
		
		SimpleRigidBody<String> srb = new SimpleCharacterControl<String>(entity, physicsController, "Geometry_" + name);
		this.physicsController.addSimpleRigidBody(srb);

		return srb;
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
