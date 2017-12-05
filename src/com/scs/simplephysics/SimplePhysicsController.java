package com.scs.simplephysics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.entities.PhysicalEntity;

public class SimplePhysicsController<T> {

	public static final float MIN_MOVE_DIST = 0.0001f;
	public static final float MAX_MOVE_DIST = 0.5f;
	public static final float DEFAULT_AERODYNAMICNESS = 0.99f;
	public static final float DEFAULT_GRAVITY = -4f;

	private ArrayList<SimpleRigidBody<T>> entities = new ArrayList<>();
	private ICollisionListener<T> collListener;
	private boolean enabled = true;

	// Settings
	private float gravity;
	private float aerodynamicness;

	public SimplePhysicsController(ICollisionListener<T> _collListener) {
		this(_collListener, DEFAULT_GRAVITY, DEFAULT_AERODYNAMICNESS);
	}


	public SimplePhysicsController(ICollisionListener<T> _collListener, float _gravity, float _aerodynamicness) {
		super();

		collListener = _collListener;
		gravity = _gravity;
		aerodynamicness = _aerodynamicness;
	}


	public ICollisionListener<T> getCollisionListener() {
		return this.collListener;
	}


	public void setEnabled(boolean b) {
		this.enabled = b;
	}


	public Collection<SimpleRigidBody<T>> getEntities() {
		return Collections.synchronizedCollection(this.entities);
	}


	public void addSimpleRigidBody(SimpleRigidBody<T> srb) {
		synchronized (entities) {
			this.entities.add(srb);
		}
	}


	public void removeSimpleRigidBody(SimpleRigidBody<T> srb) {
		synchronized (entities) {
			this.entities.remove(srb);
		}
	}


	/*
	 * You can either call this method, or call SimpleRigidBody.proces() on each entity.
	 */
	public void update(float tpf_secs) {
		if (this.enabled) {
			synchronized (entities) {
				Iterator<SimpleRigidBody<T>> it = this.entities.iterator();
				while (it.hasNext()) {
					SimpleRigidBody<T> srb = it.next();
					srb.process(tpf_secs);
				}
			}
		}
	}


	public boolean getEnabled() {
		return this.enabled;
	}


	public float getGravity() {
		return this.gravity;
	}


	public float getAerodynamicness() {
		return this.aerodynamicness;
	}


	public CollisionResults checkForCollisions(Ray r) { // todo - use this?
		CollisionResults res = new CollisionResults();
		synchronized (entities) {
			Iterator<SimpleRigidBody<T>> it = this.entities.iterator();
			while (it.hasNext()) {
				SimpleRigidBody<T> srb = it.next();
				//r.collideWith(ic.getMainNode().getWorldBound(), res);
				srb.getSpatial().collideWith(r, res);
			}
		}
		return res;
	}




}
