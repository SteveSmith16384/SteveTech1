package com.scs.simplephysics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.jme3.bounding.BoundingBox;

import jdk.nashorn.internal.objects.Global;

public class SimplePhysicsController<T> {

	public static final boolean USE_NEW_COLLISION_METHOD = true; // todo - remove
	public static final boolean DEBUG = false;

	public static final float MIN_MOVE_DIST = 0.001f;
	public static final float DEFAULT_AERODYNAMICNESS = 0.99f; // Prevent things moving forever
	public static final float DEFAULT_GRAVITY = -5f;

	private ArrayList<SimpleRigidBody<T>> entities = new ArrayList<>();
	private ICollisionListener<T> collListener;
	private boolean enabled = true;

	// Settings
	private float gravity;
	private float aerodynamicness;

	// Efficiency
	private int nodeSize; // The size of the node collections
	public HashMap<String, SimpleNode<T>> nodes;
	public ArrayList<SimpleRigidBody<T>> movingEntities;

	public SimplePhysicsController(ICollisionListener<T> _collListener, int _nodeSize) {
		this(_collListener, _nodeSize, DEFAULT_GRAVITY, DEFAULT_AERODYNAMICNESS);
	}


	/**
	 * 
	 * @param _collListener
	 * @param _nodeSizeXZ
	 * @param _nodeSizeY
	 * @param _gravity
	 * @param _aerodynamicness
	 */
	public SimplePhysicsController(ICollisionListener<T> _collListener, int _nodeSize, float _gravity, float _aerodynamicness) {
		super();

		collListener = _collListener;
		nodeSize = _nodeSize;
		gravity = _gravity;
		aerodynamicness = _aerodynamicness;

		if (USE_NEW_COLLISION_METHOD) {
			nodes = new HashMap<String, SimpleNode<T>>();
			movingEntities = new ArrayList<SimpleRigidBody<T>>();
		}
	}


	public ICollisionListener<T> getCollisionListener() {
		return this.collListener;
	}


	public void setEnabled(boolean b) {
		this.enabled = b;
	}


	public List<SimpleRigidBody<T>> getEntities() {
		return this.entities;
	}


	public void removeAllEntities() {
		synchronized (entities) {
			this.entities.clear();
		}
		nodes.clear();
	}


	public void addSimpleRigidBody(SimpleRigidBody<T> srb) {
		if (srb == null) {
			throw new RuntimeException("SimpleRigidBody is null");
		}
		
		if (this.entities.contains(srb)) {
			throw new RuntimeException("SRB already added");
		}

		synchronized (entities) {
			this.entities.add(srb);
		}

		if (USE_NEW_COLLISION_METHOD) {
			BoundingBox bb = srb.getBoundingBox();
			boolean tooBig = bb.getXExtent() > nodeSize || bb.getYExtent() > nodeSize || bb.getZExtent() > nodeSize;
			if (srb.getNeverMoves() && this.nodeSize > 0 && !tooBig) {
				int x = (int)bb.getCenter().x / this.nodeSize;
				int y = (int)bb.getCenter().y / this.nodeSize;
				int z = (int)bb.getCenter().z / this.nodeSize;

				String id = x + "_" + y + "_" + z;
				if (!this.nodes.containsKey(id)) {
					SimpleNode<T> node = new SimpleNode<T>(id);
					this.nodes.put(id, node);
				}
				SimpleNode<T> n = this.nodes.get(id);
				n.add(srb);			
			} else {
				movingEntities.add(srb);
			}
		}

		srb.removed = false;
		//srb.setCurrentGravInc = 0;
		
		if (srb.movedByForces()) {
			// Check to see if they're not already colliding
			/*SimpleRigidBody<T> tmpWasCollision = srb.checkForCollisions();
			if (tmpWasCollision != null) {
				System.err.println("Warning: " + this + " has collided immediately with " + tmpWasCollision.userObject);
			}*/
		}
	}


	public void removeSimpleRigidBody(SimpleRigidBody<T> srb) {
		synchronized (entities) {
			this.entities.remove(srb);
		}
		if (USE_NEW_COLLISION_METHOD) {
			srb.removeFromParent_INTERNAL();
			this.movingEntities.remove(srb);
		}
		srb.removed = true;
	}


	public void update(float tpf_secs) {
		if (tpf_secs > 0.1f) {
			tpf_secs = 0.1f; // Prevent stepping too far
		}

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


	public static void p(String s) {
		System.out.println(s);
	}


}
