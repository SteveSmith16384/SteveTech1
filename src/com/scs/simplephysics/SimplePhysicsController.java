package com.scs.simplephysics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class SimplePhysicsController<T> {

	private ArrayList<SimpleRigidBody<T>> entities = new ArrayList<>();
	private ICollisionListener<T> collListener;
	private boolean enabled = true;
	//private Vector3f lowerBounds, upperBounds;
	
	public SimplePhysicsController(ICollisionListener<T> _collListener) {//, Vector3f _lowerBounds, Vector3f _upperBounds) {
		super();

		collListener = _collListener;
		//lowerBounds = _lowerBounds;
		//upperBounds = _upperBounds;
	}

	
	/*public void setBounds(Vector3f _lowerBounds, Vector3f _upperBounds) {
		lowerBounds = _lowerBounds;
		upperBounds = _upperBounds;
	}
	
	
	public boolean isWithinBounds(Vector3f pos) {
		if (this.lowerBounds != null && this.upperBounds != null) {
			return pos.x >= lowerBounds.x && pos.y >= lowerBounds.y && pos.z >= lowerBounds.z && pos.x <= upperBounds.x && pos.y <= upperBounds.y && pos.z <= upperBounds.z;
		}
		return true;
	}*/
	

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
				Iterator<SimpleRigidBody<T>> it = this.entities.iterator();
				//for (SimpleRigidBody<T> srb : this.entities) {
				while (it.hasNext()) {
					SimpleRigidBody<T> srb = it.next();
					/*Spatial s = srb.getSpatial();
					if (!this.isWithinBounds(s.getWorldTranslation())) {
						it.remove();
						continue;
					}*/
					srb.process(tpf_secs);
				}
			}
		}
	}
	
	
	//public d
	
}
