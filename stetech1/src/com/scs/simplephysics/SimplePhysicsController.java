package com.scs.simplephysics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimplePhysicsController<T> {

	public static final float MIN_MOVE_DIST = 0.001f;
	public static final float MAX_MOVE_DIST = 999f;//0.5f;
	public static final float DEFAULT_AERODYNAMICNESS = 0.99f; // Prevent things moving forever
	public static final float DEFAULT_GRAVITY = -5f;

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

/*
	public Collection<SimpleRigidBody<T>> getEntities() {
		return Collections.synchronizedCollection(this.entities);
	}
*/

	public List<SimpleRigidBody<T>> getEntities() {
		return this.entities;
	}


	public void removeAllEntities() {
		synchronized (entities) {
			this.entities.clear();
		}
	}
	
	
	public void addSimpleRigidBody(SimpleRigidBody<T> srb) {
		if (srb == null) {
			throw new RuntimeException("Todo");
		}
		synchronized (entities) {
			this.entities.add(srb);
		}
	}


	public void removeSimpleRigidBody(SimpleRigidBody<T> srb) {
		synchronized (entities) {
			//if (DEBUG_EN)
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

}
