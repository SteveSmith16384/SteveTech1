package com.scs.simplephysics.tests;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.scs.simplephysics.ICollisionListener;
import com.scs.simplephysics.ISimpleEntity;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;

/**
 * test putting a box in the corner of a room, colliding with both walls, to see if it corrects its position properly. 
 * @author StephenCS
 *
 */
public class TestBoxInCorner implements ICollisionListener<String> {

	private static final float LOOP_INTERVAL_SECS = .001f;
	private static final int REPORT_INTERVAL_SECS = 1;
	private static final float TOTAL_DURATION_SECS = 10;
	
	private SimplePhysicsController<String> physicsController;

	public static void main(String args[]) {
		new TestBoxInCorner();
	}


	private TestBoxInCorner() {
		physicsController = new SimplePhysicsController<String>(this, -1, -1, 0, 0.99f);

		Geometry wallLR = this.createBox("wallLR", new Vector3f(10f, 10f, .5f), new Vector3f(0, 0, 0), false);
		Geometry wallFB = this.createBox("wallFB", new Vector3f(.5f, 10f, 10f), new Vector3f(0, 0, 0), false);
		Geometry box = this.createBox("box", new Vector3f(.5f, .5f, .5f), new Vector3f(.5f, 0, .5f), true);
		
		float time = 1;
		int prevReport = 0;
		while (time <= TOTAL_DURATION_SECS) {
			this.physicsController.update(LOOP_INTERVAL_SECS);
			if (time > prevReport) {
				p("Box position: " + box.getWorldTranslation());
				prevReport += REPORT_INTERVAL_SECS;
			}
			time += LOOP_INTERVAL_SECS;
		}
	}


	private Geometry createBox(String name, Vector3f size, Vector3f pos, boolean moves) {
		Box box = new Box(size.x, size.y, size.z);
		final Geometry boxGeometry = new Geometry(name, box);
		boxGeometry.setLocalTranslation(pos); // origin is the middle
		ISimpleEntity<String> entity = new SimpleEntityHelper<String>(boxGeometry);
		
		SimpleRigidBody<String> srb = new SimpleRigidBody<String>(entity, physicsController, moves, "Geometry_" + name);
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
