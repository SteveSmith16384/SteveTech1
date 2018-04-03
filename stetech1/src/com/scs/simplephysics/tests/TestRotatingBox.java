package com.scs.simplephysics.tests;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.scs.simplephysics.ICollisionListener;
import com.scs.simplephysics.ISimpleEntity;
import com.scs.simplephysics.SimpleCharacterControl;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;

/**
 * Test what happens when a box rotates, since this will increase the size of the bounding box.
 * @author StephenCS
 *
 */
public class TestRotatingBox implements ICollisionListener<String> {

	private static final float LOOP_INTERVAL_SECS = .001f;
	private static final int REPORT_INTERVAL_SECS = 1;
	private static final float TOTAL_DURATION_SECS = 10;
	
	private SimplePhysicsController<String> physicsController;

	public static void main(String args[]) {
		new TestRotatingBox();
	}


	private TestRotatingBox() {
		physicsController = new SimplePhysicsController<String>(this, -1, -1, 0, 0.99f);

		Geometry box1 = this.createBox("box1", new Vector3f(0, 0, 0));
		Geometry box3 = this.createBox("box3", new Vector3f(1.01f, 0, 0));
		
		p("First run...");
		this.physicsController.update(LOOP_INTERVAL_SECS);
		
		p("Rotating box...");
		box1.lookAt(box1.getLocalTranslation().add(new Vector3f(1, 0, 1)), Vector3f.UNIT_Y); // Look 45 degrees
		
		p("Second run...");
		this.physicsController.update(LOOP_INTERVAL_SECS);

		p("Final positions...");
		p("Box 1: " + box1.getWorldTranslation());
		p("Box 3: " + box3.getWorldTranslation());
	}


	private Geometry createBox(String name, Vector3f pos) {
		Box box = new Box(.5f, .5f, .5f);
		final Geometry boxGeometry = new Geometry(name, box);
		boxGeometry.setLocalTranslation(pos); // origin is the middle
		ISimpleEntity<String> entity = new SimpleEntityHelper<String>(boxGeometry);
		
		SimpleRigidBody<String> srb = new SimpleRigidBody<String>(entity, physicsController, true, "Geometry_" + name);
		this.physicsController.addSimpleRigidBody(srb);

		return boxGeometry;
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
		p("Collision between " + a.userObject + " and " + b.userObject);
		//a.checkSRBvSRB(b); // todo - remove this
	}


}
