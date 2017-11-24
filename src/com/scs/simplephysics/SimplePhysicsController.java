package com.scs.simplephysics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class SimplePhysicsController {
	
	private ArrayList<SimpleRigidBody> entities = new ArrayList<>();
	public ICollisionListener collListener; // todo - make private
	public boolean enabled = true;
	
	public SimplePhysicsController(ICollisionListener _collListener) {
		super();
		
		collListener = _collListener;
	}

	
	public Collection<SimpleRigidBody> getEntities() {
		return Collections.synchronizedCollection(this.entities);
		//return this.entities.values().iterator();
	}


	public void addSimpleRigidBody(SimpleRigidBody srb) {
		this.entities.add(srb); // todo - remove
	}
	
	
	public void update(float tpf_secs) {
		if (this.enabled) {
		for (SimpleRigidBody srb : this.entities) {
			srb.process(tpf_secs);
		}
		}
	}
}
