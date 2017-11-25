package com.scs.simplephysics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class SimplePhysicsController {

	private ArrayList<SimpleRigidBody> entities = new ArrayList<>();
	private ICollisionListener collListener;
	private boolean enabled = true;
	//private Vector3f bounds

	public SimplePhysicsController(ICollisionListener _collListener) {
		super();

		collListener = _collListener;
	}


	public ICollisionListener getCollisionListener() {
		return this.collListener;
	}


	public void setEnabled(boolean b) {
		this.enabled = b;
	}


	public Collection<SimpleRigidBody> getEntities() {
		return Collections.synchronizedCollection(this.entities);
		//return this.entities.values().iterator();
	}


	public void addSimpleRigidBody(SimpleRigidBody srb) {
		synchronized (entities) {
			this.entities.add(srb);
		}
	}


	public void removeSimpleRigidBody(SimpleRigidBody srb) {
		synchronized (entities) {
			this.entities.remove(srb);
		}
	}


	public void update(float tpf_secs) {
		if (this.enabled) {
			synchronized (entities) {
				for (SimpleRigidBody srb : this.entities) {
					srb.process(tpf_secs);
				}
			}
		}
	}
}
