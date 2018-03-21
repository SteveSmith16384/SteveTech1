package com.scs.stevetech1.entities;

import java.util.HashMap;
import java.util.Iterator;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.scs.simplephysics.ISimpleEntity;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IPhysicalEntity;
import com.scs.stevetech1.components.IProcessByServer;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.server.RayCollisionData;
import com.scs.stevetech1.shared.EntityPositionData;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.shared.PositionCalculator;

public abstract class PhysicalEntity extends Entity implements IPhysicalEntity, IProcessByServer, ISimpleEntity<PhysicalEntity> {

	protected Node mainNode;
	public SimpleRigidBody<PhysicalEntity> simpleRigidBody;
	public PositionCalculator serverPositionData; // Used client side for all entities (for position interpolation), and server side for Avatars, for rewinding position
	public boolean collideable = true;

	//private Vector3f prevPos = new Vector3f(-100, -100, -100); // offset to ensure the first sendUpdates check returns true
	//private Quaternion prevRot = new Quaternion();

	// Rewind settings
	private Vector3f originalPos = new Vector3f();
	private Quaternion originalRot = new Quaternion();

	public boolean sendPositionUpdate = true; // Send first time
	private boolean requiresProcessing;
	public Node owner;

	public PhysicalEntity(IEntityController _game, int id, int type, String _name, boolean _requiresProcessing) {
		super(_game, id, type, _name);

		requiresProcessing = _requiresProcessing;

		serverPositionData = new PositionCalculator(true, 100);
		mainNode = new Node(name + "_MainNode_" + id);
	}


	@Override
	public void processByServer(AbstractEntityServer server, float tpf_secs) {
		if (this instanceof AbstractAvatar) {
			throw new RuntimeException("Do not call this for avatars!");
		}

		if (simpleRigidBody != null) {
			simpleRigidBody.process(tpf_secs);
		}

		if (getWorldTranslation().y < -1) {
			// Dropped away?
			Globals.p(getName() + " has fallen off the edge");
			fallenOffEdge();
		}

		if (this instanceof IRewindable) {
			addPositionData();
		}
	}


	/*
	 * Called by the server 
	 */
	protected void addPositionData() {
		// Store the position for use when rewinding.
		//EntityPositionData epd = new EntityPositionData(this.getWorldTranslation().clone(), this.getWorldRotation(), System.currentTimeMillis());
		this.serverPositionData.addPositionData(this.getWorldTranslation(), this.getWorldRotation(), System.currentTimeMillis());
	}


	/*
	 * Called by the client 
	 */
	public void addPositionData(Vector3f pos, Quaternion q, long time) {
		this.serverPositionData.addPositionData(pos, q, time);	
	}


	// This is overridden by client avatars to take into account local position
	public void calcPosition(AbstractGameClient mainApp, long serverTimeToUse, float tpf_secs) {
		EntityPositionData epd = serverPositionData.calcPosition(serverTimeToUse, false);
		if (epd != null) {
			this.setWorldTranslation(epd.position);
			this.setWorldRotation(epd.rotation);
		} else {
			//Settings.p("No position data for " + this);
		}

	}


	public void clearPositiondata() {
		this.serverPositionData.clear();
	}


	@Override
	public void remove() {
		super.remove();

		if (simpleRigidBody != null) {
			this.game.getPhysicsController().removeSimpleRigidBody(simpleRigidBody);
		}

		if (this.mainNode.getParent() != null) { // Unlaunched bullets have no parent
			this.mainNode.removeFromParent();
		}
	}


	public Node getMainNode() {
		return mainNode;
	}


	public float distance(PhysicalEntity o) {
		//return distance(o.getMainNode().getWorldTranslation());
		return distance(o.getWorldTranslation());
	}


	public float distance(Vector3f pos) {
		float dist = this.getWorldTranslation().distance(pos);
		return dist;
	}


	public Vector3f getWorldTranslation() {
		//return this.main_node.getWorldTranslation();  // 000?
		return this.getMainNode().getLocalTranslation();
	}


	public void setWorldTranslation(Vector3f pos) {
		//this.getMainNode().setLocalTranslation(pos.x, pos.y, pos.z);
		this.setWorldTranslation(pos.x, pos.y, pos.z);
	}


	public void setWorldTranslation(float x, float z) {
		//this.getMainNode().setLocalTranslation(x, this.getWorldTranslation().y, z);
		this.setWorldTranslation(x, this.getWorldTranslation().y, z);
	}


	public void setWorldTranslation(float x, float y, float z) {
		this.getMainNode().setLocalTranslation(x, y, z);
		sendPositionUpdate = true;
	}


	public void applyOneOffForce(Vector3f dir) {
		this.simpleRigidBody.getLinearVelocity().addLocal(dir);
	}


	public Quaternion getWorldRotation() {
		return this.getMainNode().getLocalRotation();
	}


	public void setWorldRotation(final Quaternion newRot2) {
		getMainNode().setLocalRotation(newRot2);
		this.sendPositionUpdate = true;
	}


	public boolean sendUpdates() {
		return this.sendPositionUpdate;
	}
	
	/*
	public boolean sendUpdates_OLD() {
		if (!this.moves) { // todo - scs new!
			return false;
		}

		Vector3f currentPos = this.getWorldTranslation();
		float dist = currentPos.distance(prevPos);
		boolean hasMoved = dist >= Globals.SMALLEST_MOVE_DIST; 
		if (hasMoved) {
			this.prevPos.set(currentPos);
		}

		if (!hasMoved) {
			// Check if rotation changed
			Quaternion currentRot = this.getWorldRotation(); //prevRot.subtract(currentRot);
			boolean rotChanged = !currentRot.equals(this.prevRot); // todo - check diff!
			if (rotChanged) {
				prevRot.set(currentRot);
			}
			hasMoved = rotChanged;
		}

		return hasMoved;
	}
*/

	@Override
	public String toString() {
		return super.toString();
	}


	public void rewindPositionTo(long serverTimeToUse) { //this.getWorldTranslation()
		EntityPositionData shooterEPD = this.serverPositionData.calcPosition(serverTimeToUse, true);
		if (shooterEPD != null) {
			this.originalPos.set(this.getWorldTranslation());
			this.originalRot.set(this.getWorldRotation());
			this.setWorldTranslation(shooterEPD.position);
			this.setWorldRotation(shooterEPD.rotation);
		} else {
			Globals.p("Unable to rewind position: no data");
		}
	}


	public void restorePosition() {
		this.setWorldTranslation(this.originalPos);
		this.setWorldRotation(this.originalRot);
		this.mainNode.updateGeometricState();
	}


	@Override
	public void adjustWorldTranslation(Vector3f offset) {
		this.setWorldTranslation(this.getWorldTranslation().add(offset));

	}


	public void fallenOffEdge() {
		// Override for avatars
		this.remove();
	}


	public RayCollisionData checkForCollisions(Ray r, float range) {
		CollisionResults res = new CollisionResults();
		int c = game.getGameNode().collideWith(r, res);
		if (c == 0) {
			Globals.p("No Ray collisions");
			return null;
		}
		Iterator<CollisionResult> it = res.iterator();
		while (it.hasNext()) {
			CollisionResult col = it.next();
			if (col.getDistance() > range) {
				break;
			}
			Spatial s = col.getGeometry();
			while (s.getUserData(Globals.ENTITY) == null) {
				s = s.getParent();
				if (s == null) {
					break;
				}
			}
			if (s != null && s.getUserData(Globals.ENTITY) != null) {
				PhysicalEntity pe = (PhysicalEntity)s.getUserData(Globals.ENTITY);
				if (pe != this && pe.collideable) {
					//Settings.p("Ray collided with " + s + " at " + col.getContactPoint());
					return new RayCollisionData(pe, col.getContactPoint(), col.getDistance());
				}
			}
		}

		return null;
	}


	@Override
	public HashMap<String, Object> getCreationData() {
		if (creationData == null) {
			//creationData = new HashMap<String, Object>();
			throw new RuntimeException("creationData needs creating for " + this.getName());
		}
		creationData.put("pos", this.getWorldTranslation());
		creationData.put("quat", this.getWorldRotation());
		return super.getCreationData();
	}


	public boolean canSee(PhysicalEntity target, float range) {
		// Test the ray from the middle of the entity
		Ray r = new Ray(this.getMainNode().getWorldBound().getCenter(), target.getMainNode().getWorldBound().getCenter().subtract(this.getMainNode().getWorldBound().getCenter()).normalizeLocal());
		r.setLimit(range);
		CollisionResults res = new CollisionResults();
		int c = game.getGameNode().collideWith(r, res);
		if (c == 0) {
			//Globals.p("No Ray collisions?!");
			return false;
		}
		Iterator<CollisionResult> it = res.iterator();
		while (it.hasNext()) {
			CollisionResult col = it.next();
			if (col.getDistance() > range) {
				return false;
			}
			Spatial s = col.getGeometry();
			while (s.getUserData(Globals.ENTITY) == null) {
				s = s.getParent();
				if (s == null) {
					break;
				}
			}
			if (s != null && s.getUserData(Globals.ENTITY) != null) {
				PhysicalEntity pe = (PhysicalEntity)s.getUserData(Globals.ENTITY);
				if (pe != this && pe.collideable) {
					//Settings.p("Ray collided with " + s + " at " + col.getContactPoint());
					return pe == target;
				}
			}
		}

		return false;
	}


	@Override
	public final boolean requiresProcessing() {
		return requiresProcessing;
	}
	
	
	public Node getOwnerNode() {
		return owner; // Override if the entity should have a different parent node to the default 
	}


	@Override
	public Spatial getSpatial() {
		return this.mainNode;
	}


	@Override
	public void hasMoved() {
		this.sendPositionUpdate = true;
		
	}


}
