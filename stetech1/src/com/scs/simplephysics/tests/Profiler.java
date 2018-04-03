package com.scs.simplephysics.tests;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.scs.simplephysics.ICollisionListener;
import com.scs.simplephysics.ISimpleEntity;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;

/**
 * This creates a "chessboard" of blocks, and a ball above each block.  Each ball will fall and bounce on the block below it.
 *  
 * @author stephencs
 *
 */
public class Profiler implements ICollisionListener<String> {

	private static final int SIZE = 10;
	private static final float LOOP_INTERVAL_SECS = 1f/50; // FPS
	private static final int TOTAL_ITERATIONS = 1000;

	private SimplePhysicsController<String> physicsController;

	public static void main(String args[]) {
		new Profiler();
	}


	private Profiler() {
		physicsController = new SimplePhysicsController<String>(this, SIZE/2, 1);

		this.createBalls();
		this.createBoxes();
		//this.createFloor();

		p("Started...");
		long start = System.currentTimeMillis();
		for (int i=0 ; i<TOTAL_ITERATIONS ; i++) {
			long itStart = System.currentTimeMillis();

			this.physicsController.update(LOOP_INTERVAL_SECS);

			long itDuration = System.currentTimeMillis() - itStart;
			p("Iteration " + i + ": Took " + itDuration + "ms");
		}
		long totalDuration = System.currentTimeMillis() - start;

		// List entities
		if (SimplePhysicsController.DEBUG) {
			for (SimpleRigidBody<String> e : this.physicsController.getEntities()) {
				if (e.movedByForces()) {
					p(e.toString() + " is at " + e.getBoundingBox().getCenter());
				}
			}
		}

		p("Finished.  Took " + totalDuration);
	}


	private void createBalls() {
		int num = 0;
		for (int y=0 ; y<SIZE ; y++) {
			for (int x=0 ; x<SIZE ; x++) {
				//int x = 0, y = 0;
				Sphere sphere = new Sphere(8, 8, .1f);
				final Geometry ballGeometry = new Geometry("Sphere_" + x + "_" + y, sphere);
				ballGeometry.setLocalTranslation(x+.5f, 10f, y+.5f); // origin is the middle
				ISimpleEntity<String> entity = new SimpleEntityHelper<String>(ballGeometry);/* new ISimpleEntity<String>() {

					@Override
					public void move(Vector3f pos) {
						ballGeometry.move(pos);
						
					}

					@Override
					public BoundingBox getBoundingBox() {
						return (BoundingBox)ballGeometry.getWorldBound();
					}

					@Override
					public void hasMoved() {
						// Do nothing
					}


				};*/
				SimpleRigidBody<String> srb = new SimpleRigidBody<String>(entity, physicsController, true, "ballGeometry_" + x + "_" + y);
				this.physicsController.addSimpleRigidBody(srb);

				num++;
			}
		}
	}


	private void createBoxes() {
		int num = 0;
		for (int y=0 ; y<SIZE ; y++) {
			for (int x=0 ; x<SIZE ; x++) {
				Box box = new Box(.5f, .5f, .5f);
				final Geometry boxGeometry = new Geometry("Box_" + x + "_" + y, box);
				boxGeometry.setLocalTranslation(x+.5f, -.5f, y+.5f); // origin is the middle
				ISimpleEntity<String> entity = new SimpleEntityHelper<String>(boxGeometry);
				SimpleRigidBody<String> srb = new SimpleRigidBody<String>(entity, physicsController, false, "boxGeometry_" + x + "_" + y);
				this.physicsController.addSimpleRigidBody(srb);

				num++;
			}
		}
	}


	private void createFloor() {
		Box floor = new Box(SIZE, 1, SIZE);
		final Geometry floorGeometry = new Geometry("floor", floor);
		floorGeometry.setLocalTranslation(SIZE/2, -2, SIZE/2); 
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
	public void collisionOccurred(SimpleRigidBody<String> a, SimpleRigidBody<String> b) {
		//p("Collision between " + a.userObject + " and " + b.userObject);
	}


}
