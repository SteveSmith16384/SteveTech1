package com.scs.simplephysics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.jme3.scene.Spatial;

public class SimplePhysicsController<T> {

	private ArrayList<SimpleRigidBody<T>> entities = new ArrayList<>();
	private ICollisionListener<T> collListener;
	private boolean enabled = true;
	//private Vector3f bounds

	public SimplePhysicsController(ICollisionListener<T> _collListener) {
		super();

		collListener = _collListener;
	}


	public ICollisionListener<T> getCollisionListener() {
		return this.collListener;
	}


	public void setEnabled(boolean b) {
		this.enabled = b;
	}


	public Collection<SimpleRigidBody<T>> getEntities() {
		return Collections.synchronizedCollection(this.entities);
		//return this.entities.values().iterator();
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


	public void update(float tpf_secs) {
		if (this.enabled) {
			synchronized (entities) {
				for (SimpleRigidBody<T> srb : this.entities) {
					srb.process(tpf_secs);
				}
			}
		}
	}
	
}
