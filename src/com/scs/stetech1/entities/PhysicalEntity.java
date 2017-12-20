package com.scs.stetech1.entities;

import java.util.HashMap;
import java.util.Iterator;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stetech1.client.AbstractGameClient;
import com.scs.stetech1.components.IPhysicalEntity;
import com.scs.stetech1.components.IProcessByServer;
import com.scs.stetech1.components.IRewindable;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.RayCollisionData;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityPositionData;
import com.scs.stetech1.shared.IEntityController;
import com.scs.stetech1.shared.PositionCalculator;

public abstract class PhysicalEntity extends Entity implements IPhysicalEntity, IProcessByServer {

	protected Node mainNode;
	public SimpleRigidBody<PhysicalEntity> simpleRigidBody;
	protected PositionCalculator serverPositionData; // Used client side for all entities (for position interpolation), and server side for Avatars, for rewinding position
	public boolean collideable = true;
	
	private Vector3f prevPos = new Vector3f(-100, -100, -100); // offset to ensure the first hasMoved check returns true
	private Quaternion prevRot = new Quaternion();

	// Rewind settings
	private Vector3f originalPos = new Vector3f();
	private Quaternion originalRot = new Quaternion();

	public PhysicalEntity(IEntityController _game, int id, int type, String _name) {
		super(_game, id, type, _name);

		serverPositionData = new PositionCalculator(true, 100);
		mainNode = new Node(name + "_MainNode");
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		if (this instanceof AbstractAvatar) {
			throw new RuntimeException("Do not call this for avatars!");
		}
		
		if (simpleRigidBody != null) {
			simpleRigidBody.process(tpf_secs);
		}

		if (getWorldTranslation().y < -1) {
			// Dropped away?
			server.console.appendText(getName() + " has fallen off the edge");
			Settings.p(getName() + " has fallen off the edge");
			fallenOffEdge();
		}

		if (this instanceof IRewindable) {
			addPositionData();
		}
	}


	protected void addPositionData() {
		// Store the position for use when rewinding.
		//EntityPositionData epd = new EntityPositionData(this.getWorldTranslation().clone(), this.getWorldRotation(), System.currentTimeMillis());
		this.serverPositionData.addPositionData(this.getWorldTranslation(), this.getWorldRotation(), System.currentTimeMillis());
	}

/*
	public void addPositionData(EntityPositionData epd) {
		this.serverPositionData.addPositionData(epd);
	}
*/
	
	public void addPositionData(Vector3f pos, Quaternion q, long time) {
		this.serverPositionData.addPositionData(pos, q, time);	
	}

	
	// This is overridden by Avatars to take into account local position
	public void calcPosition(AbstractGameClient mainApp, long serverTimeToUse) {
		EntityPositionData epd = serverPositionData.calcPosition(serverTimeToUse);
		if (epd != null) {
			this.setWorldTranslation(epd.position);
			this.setWorldRotation(epd.rotation);
		} else {
			//Settings.p("No position data for " + this);
		}

	}


	public void clearPositiondata() {
		this.serverPositionData.clearPositiondata();
	}


	public void setWorldRotation(final Quaternion newRot2) {
		getMainNode().setLocalRotation(newRot2);
	}


	@Override
	public void remove() {
		super.remove();
		if (this.mainNode.getParent() == null) {
			//throw new RuntimeException("No parent!");
		} else {
			this.mainNode.removeFromParent(); // Don't need to remove left/right nodes as they are attached to the main node
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


	/*	public RayCollisionData calcHitEntity(Vector3f shootDir, float range) {
		Vector3f from = this.getWorldTranslation().add(shootDir.mult(1f)); // Prevent us shooting ourselves
		AbstractGameServer server = (AbstractGameServer)game;
		Ray ray = new Ray(from, shootDir);
		return server.checkForCollisions(ray);
	}
	 */

	public Vector3f getWorldTranslation() {
		//return this.main_node.getWorldTranslation();  // 000?
		return this.getMainNode().getLocalTranslation();
	}


	public void setWorldTranslation(Vector3f pos) {
		this.getMainNode().setLocalTranslation(pos.x, pos.y, pos.z);
	}


	public void applyOneOffForce(Vector3f dir) {
		this.simpleRigidBody.getLinearVelocity().addLocal(dir);
	}


	public Quaternion getWorldRotation() {
		return this.getMainNode().getLocalRotation();
	}


	public boolean hasMoved() {
		Vector3f currentPos = this.getWorldTranslation();
		float dist = currentPos.distance(prevPos);
		boolean hasMoved = dist > 0.01f; 
		if (hasMoved) {
			/*if (dist > 10f) {
				Settings.p(this.toString() + " has moved A LOT " + dist);
			}*/
			//Settings.p(this.toString() + " has moved " + dist);
			this.prevPos.set(currentPos);
		}

		// Check if rotation changed
		/*todo Quaternion currentRot = this.getWorldRotation(); //prevRot.subtract(currentRot);
		boolean rotChanged = !currentRot.equals(this.prevRot); // todo - check diff!
		if (rotChanged) {
			prevRot.set(currentRot);
		}
		hasMoved = hasMoved || rotChanged;*/

		return hasMoved;
	}


	@Override
	public String toString() {
		return super.toString();
	}


	public void rewindPositionTo(long serverTimeToUse) { //this.getWorldTranslation()
		EntityPositionData shooterEPD = this.serverPositionData.calcPosition(serverTimeToUse);
		if (shooterEPD != null) {
			this.originalPos.set(this.getWorldTranslation());
			this.originalRot.set(this.getWorldRotation());
			this.setWorldTranslation(shooterEPD.position);
			this.setWorldRotation(shooterEPD.rotation);
		} else {
			Settings.p("Unable to rewind position: no data");
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
		int c = game.getRootNode().collideWith(r, res);
		if (c == 0) {
			Settings.p("No Ray collisions");
			return null;
		}
		Iterator<CollisionResult> it = res.iterator();
		while (it.hasNext()) {
			CollisionResult col = it.next();
			if (col.getDistance() > range) {
				break;
			}
			Spatial s = col.getGeometry();
			while (s == null || s.getUserData(Settings.ENTITY) == null) {
				s = s.getParent();
			}
			if (s != null && s.getUserData(Settings.ENTITY) != null) {
				PhysicalEntity pe = (PhysicalEntity)s.getUserData(Settings.ENTITY);
				if (pe != this && pe.collideable) {
					//Settings.p("Ray collided with " + s + " at " + col.getContactPoint());
					return new RayCollisionData(pe, col.getContactPoint(), col.getDistance());
				}
			}
		}

		return null;
	}
	
	
	public boolean canMove() {
		return true;
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


}
