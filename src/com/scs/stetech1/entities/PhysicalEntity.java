package com.scs.stetech1.entities;

import java.util.HashMap;
import java.util.List;

import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.scs.stetech1.client.GenericClient;
import com.scs.stetech1.components.IPhysicalEntity;
import com.scs.stetech1.components.IProcessByServer;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityPositionData;
import com.scs.stetech1.shared.HitData;
import com.scs.stetech1.shared.IEntityController;
import com.scs.stetech1.shared.PositionCalculator;

public abstract class PhysicalEntity extends Entity implements IPhysicalEntity, IProcessByServer {//, IProcessByClient {

	protected Node main_node;
	public RigidBodyControl rigidBodyControl;
	protected PositionCalculator serverPositionData; // Used client side for all entities (for position interpolation), and server side for Avatars, for rewinding position

	private Vector3f prevPos = new Vector3f(-100, -100, -100); // offset to ensure the first hasMoved check returns true
	private Quaternion prevRot = new Quaternion();

	// Server-only vars
	protected HashMap<String, Object> creationData;

	// Rewind settings
	private Vector3f originalPos = new Vector3f();
	private Quaternion originalRot = new Quaternion();


	public PhysicalEntity(IEntityController _game, int id, int type, String _name) {
		super(_game, id, type, _name);

		serverPositionData = new PositionCalculator(true, 100);
		main_node = new Node(name + "_MainNode");
	}


	public void addPositionData(EntityPositionData newData) {
		this.serverPositionData.addPositionData(newData);
	}


	// This is overridden by Avatars to take into account local position
	public void calcPosition(GenericClient mainApp, long serverTimeToUse) {
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
		if (rigidBodyControl != null) {
			this.game.getBulletAppState().getPhysicsSpace().remove(this.rigidBodyControl);
		}
		super.remove();
		if (this.main_node.getParent() == null) {
			//throw new RuntimeException("No parent!");
		} else {
			this.main_node.removeFromParent(); // Don't need to remove left/right nodes as they are attached to the main node
		}
	}


	public Node getMainNode() {
		return main_node;
	}


	public float distance(PhysicalEntity o) {
		//return distance(o.getMainNode().getWorldTranslation());
		return distance(o.rigidBodyControl.getPhysicsLocation());
	}


	public float distance(Vector3f pos) {
		//float dist = this.getMainNode().getWorldTranslation().distance(pos);
		float dist = this.rigidBodyControl.getPhysicsLocation().distance(pos);
		return dist;
	}


	public HitData calcHitEntity(Vector3f shootDir, float range) {
		Vector3f from = this.getWorldTranslation();
		Vector3f to = shootDir.normalize().multLocal(range).addLocal(from);
		List<PhysicsRayTestResult> results = game.getBulletAppState().getPhysicsSpace().rayTest(from, to);
		float dist = -1;
		PhysicsRayTestResult closest = null;
		for (PhysicsRayTestResult r : results) {
			if (r.getCollisionObject().getUserObject() != null) {
				if (closest == null) {
					closest = r;
				} else if (r.getHitFraction() < dist) {
					closest = r;
				}
				dist = r.getHitFraction();
			}
		}
		if (closest != null) {
			PhysicalEntity e = (PhysicalEntity)closest.getCollisionObject().getUserObject();
			Vector3f hitpoint = to.subtract(from).multLocal(closest.getHitFraction()).addLocal(from);
			Settings.p("Hit " + e + " at " + hitpoint);
			return new HitData(e, hitpoint);
		}
		return null;
	}


	public Vector3f getWorldTranslation() {
		//return this.rigidBodyControl.getPhysicsLocation();
		//return this.main_node.getWorldTranslation();  // 000?
		return this.getMainNode().getLocalTranslation();
	}


	public void setWorldTranslation(Vector3f pos) {
		// This is overridden by avatars, as they need to warp
		// this.rigidBodyControl.setPhysicsLocation(pos.clone()); Don't need this according to https://jmonkeyengine.github.io/wiki/jme3/advanced/physics.html#kinematic-vs-dynamic-vs-static
		this.getMainNode().setLocalTranslation(pos.x, pos.y, pos.z);
	}


	public void applyForce(Vector3f dir) {
		rigidBodyControl.applyImpulse(dir, Vector3f.ZERO);//.applyCentralForce(dir);
	}


	public Quaternion getWorldRotation() {
		return this.getMainNode().getLocalRotation();
	}


	public boolean canMove() {
		if (rigidBodyControl != null) {
			return this.rigidBodyControl.getMass() > 0; // 0 means static, and static doesn't move
		} else {
			return true; // ?
		}
	}


	public boolean hasMoved() {
		Vector3f currentPos = this.getWorldTranslation();
		float dist = currentPos.distance(prevPos);
		boolean hasMoved = dist > 0.01f; 
		if (hasMoved) {
			/*if (dist > 10f) {
				Settings.p(this.toString() + " has moved A LOT " + dist);
			}*/
			Settings.p(this.toString() + " has moved " + dist);
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


	public HashMap<String, Object> getCreationData() {
		return creationData;
	}


	public void rewindPosition(long serverTimeToUse) {
		EntityPositionData shooterEPD = this.serverPositionData.calcPosition(serverTimeToUse);
		if (shooterEPD != null) {
			this.originalPos.set(this.getWorldTranslation());
			this.originalRot.set(this.getWorldRotation());
			this.setWorldTranslation(shooterEPD.position);
			this.setWorldRotation(shooterEPD.rotation);
			//this.main_node.updateGeometricState();
			//return true;
			return;
		}
		//return false;
		throw new RuntimeException("Todo");
	}


	public void restorePosition() {
		this.setWorldTranslation(this.originalPos);
		this.setWorldRotation(this.originalRot);
		this.main_node.updateGeometricState();
	}


	@Override
	public void adjustWorldTranslation(Vector3f offset) {
		this.setWorldTranslation(this.getWorldTranslation().add(offset));

	}


	/*@Override
	public void process(GenericClient client, float tpf) {
		// Override if required
		// Do nothing
	}*/


}
