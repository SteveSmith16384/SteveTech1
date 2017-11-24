package com.scs.stetech1.entities;

import java.util.HashMap;

import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.collision.UnsupportedCollisionException;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stetech1.client.AbstractGameClient;
import com.scs.stetech1.components.ICollideable;
import com.scs.stetech1.components.IPhysicalEntity;
import com.scs.stetech1.components.IProcessByServer;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityPositionData;
import com.scs.stetech1.shared.HitData;
import com.scs.stetech1.shared.IEntityController;
import com.scs.stetech1.shared.PositionCalculator;

public abstract class PhysicalEntity extends Entity implements IPhysicalEntity, IProcessByServer, ICollideable {

	protected Node mainNode;
	public SimpleRigidBody simpleRigidBody;
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
		mainNode = new Node(name + "_MainNode");
	}


	@Override
	public void process(AbstractGameServer server, float tpf) {
		this.simpleRigidBody.process(tpf);
	}


	public void addPositionData(EntityPositionData newData) {
		this.serverPositionData.addPositionData(newData);
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


	public HitData calcHitEntity(Vector3f shootDir, float range) {
		Vector3f from = this.getWorldTranslation();
		//Vector3f to = shootDir.normalize().multLocal(range).addLocal(from);
		AbstractGameServer server = (AbstractGameServer)game;
		Ray ray = new Ray(from, shootDir);
		CollisionResults results = server.checkForCollisions(ray);
		//List<PhysicsRayTestResult> results = game.getBulletAppState().getPhysicsSpace().rayTest(from, to);
		/*float dist = -1;
		CollisionResult closest = null;
		for (CollisionResult r : results) {
			if (r.getCollisionObject().getUserObject() != null) {
				if (closest == null) {
					closest = r;
				} else if (r.getHitFraction() < dist) {
					closest = r;
				}
				dist = r.getHitFraction();
			}
		}*/
		CollisionResult closest = results.getClosestCollision();
		if (closest != null) {
			if (closest.getDistance() <= range) {
				PhysicalEntity e = null; // todo (PhysicalEntity)closest..getCollisionObject().getUserObject();
				Vector3f hitpoint = closest.getContactPoint();// to.subtract(from).multLocal(closest.getHitFraction()).addLocal(from);
				Settings.p("Hit " + e + " at " + hitpoint);
				return new HitData(e, hitpoint);
			}
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
		//rigidBodyControl.applyImpulse(dir, Vector3f.ZERO);//.applyCentralForce(dir);
	}


	public Quaternion getWorldRotation() {
		return this.getMainNode().getLocalRotation();
	}


	//public abstract boolean canMove();


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


	public HashMap<String, Object> getCreationData() {
		return creationData;
	}


	public void rewindPositionTo(long serverTimeToUse) {
		EntityPositionData shooterEPD = this.serverPositionData.calcPosition(serverTimeToUse);
		if (shooterEPD != null) {
			this.originalPos.set(this.getWorldTranslation());
			this.originalRot.set(this.getWorldRotation());
			this.setWorldTranslation(shooterEPD.position);
			this.setWorldRotation(shooterEPD.rotation);
			return;
		}
		throw new RuntimeException("Unable to rewind position: no data");
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


	@Override
	public int collideWith(Collidable other, CollisionResults results) throws UnsupportedCollisionException {
		return mainNode.collideWith(other, results);
	}


	@Override
	public boolean collidedWith(ICollideable other) {
		// override if required
		return false;

	}


	/*@Override
	public Node getNode() {
		return this.mainNode;
	}


	@Override
	public SimpleRigidBody getSimpleRigidBody() {
		return this.simpleRigidBody;
	}
*/

}
