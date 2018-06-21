package com.scs.simplephysics;

import java.util.ArrayList;
import java.util.List;

import com.jme3.bounding.BoundingBox;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Vector3f;

public class SimpleNode<T> {

	private List<SimpleRigidBody<T>> entities = new ArrayList<>();
	private Vector3f min = new Vector3f();
	private Vector3f max = new Vector3f();
	public String id;

	public SimpleNode(String _id) {
		id = _id;
		resetMinMax();
	}


	public void add(SimpleRigidBody<T> e) {
		this.entities.add(e);
		this.recalcBounds();
	}


	public void remove(SimpleRigidBody<T> e) {
		this.entities.remove(e);
		this.recalcBounds();
	}


	public void recalcBounds() {
		resetMinMax();
		synchronized (this.entities) {
			for (SimpleRigidBody<T> srb : this.entities) {
				BoundingBox bb = srb.getBoundingBox();
				if (bb.getCenter().x - bb.getXExtent() < min.x) {
					min.x = bb.getCenter().x - bb.getXExtent();
				}
				if (bb.getCenter().y - bb.getYExtent() < min.y) {
					min.y = bb.getCenter().y - bb.getYExtent();
				}
				if (bb.getCenter().z - bb.getZExtent() < min.z) {
					min.z = bb.getCenter().z - bb.getZExtent();
				}

				if (bb.getCenter().x + bb.getXExtent() > max.x) {
					max.x = bb.getCenter().x + bb.getXExtent();
				}
				if (bb.getCenter().y + bb.getYExtent() > max.y) {
					max.y = bb.getCenter().y + bb.getYExtent();
				}
				if (bb.getCenter().z + bb.getZExtent() > max.z) {
					max.z = bb.getCenter().z + bb.getZExtent();
				}
			}
		}
	}


	private void resetMinMax() {
		min.x = Float.MAX_VALUE;
		min.y = Float.MAX_VALUE;
		min.z = Float.MAX_VALUE;

		max.x = Float.MIN_VALUE;
		max.y = Float.MIN_VALUE;
		max.z = Float.MIN_VALUE;
	}


	public boolean intersects(BoundingBox bb) {
		if (max.x < bb.getCenter().x - bb.getXExtent()
				|| min.x > bb.getCenter().x + bb.getXExtent()) {
			return false;
		} else if (max.y < bb.getCenter().y - bb.getYExtent()
				|| min.y > bb.getCenter().y + bb.getYExtent()) {
			return false;
		} else if (max.z < bb.getCenter().z - bb.getZExtent()
				|| min.z > bb.getCenter().z + bb.getZExtent()) {
			return false;
		} else {
			return true;
		}

	}
	

	public int getCollisions(SimpleRigidBody<T> srb, List<SimpleRigidBody<T>> crs, CollisionResults tempCollisionResults) {
		int count = 0;
		BoundingBox bb = srb.getBoundingBox();
		if (this.intersects(bb)) { // Check we're inside the Node
			synchronized (this.entities) {
				for (SimpleRigidBody<T> other : this.entities) {
					if (srb.checkSRBvSRB(other, tempCollisionResults)) {
						crs.add(other);
						count++;
					}
				}
			}
		}
		return count;
	}
	
	
	public int getNumChildren() {
		return this.entities.size();
	}
	
	
	public int getCollisions(BoundingBox bb, List<SimpleRigidBody<T>> crs) {
		int count = 0;
		if (this.intersects(bb)) { // Check we're inside the Node
			synchronized (this.entities) {
				for (SimpleRigidBody<T> other : this.entities) {
					if (other.getBoundingBox().intersects(bb)) {
						crs.add(other);
						count++;
					}
				}
			}
		}
		return count;
	}
	
	

}