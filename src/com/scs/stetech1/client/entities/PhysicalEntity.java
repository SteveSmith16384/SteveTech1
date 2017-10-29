package com.scs.stetech1.client.entities;

import java.util.HashMap;
import java.util.concurrent.Callable;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.scs.stetech1.client.SorcerersClient;
import com.scs.stetech1.components.IProcessable;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityPositionData;
import com.scs.stetech1.shared.IEntityController;
import com.scs.stetech1.shared.PositionCalculator;

public abstract class PhysicalEntity extends Entity implements IProcessable {

	protected Node main_node;
	public RigidBodyControl rigidBodyControl;

	//private LinkedList<EntityPositionData> positionData = new LinkedList<>();

	private PositionCalculator posCalc;// = new PositionCalculator();
	//private Vector3f tempNewPos = new Vector3f();
	//private Quaternion tempNewRot = new Quaternion();

	private Vector3f prevPos = new Vector3f(-100, -100, -100); // offset to ensure the first hasMoved check returns true

	public PhysicalEntity(IEntityController _game, int type, String _name) {
		super(_game, type, _name);

		main_node = new Node(name + "_MainNode");
	}


	public void addPositionData(EntityPositionData newData) {
		this.posCalc.addPositionData(newData);
	}


	public void calcPosition(SorcerersClient mainApp, long serverTimeToUse) {
		posCalc.calcPosition(serverTimeToUse);

	}

	public void clearPositiondata() {
		this.posCalc.clearPositiondata();
	}


	public void scheduleNewPosition(SorcerersClient mainApp, final Vector3f newPos) {
		// Don't need to schedule it since we're in the JME thread
		/*mainApp.enqueue(new Callable<Spatial>() { // this
			public Spatial call() throws Exception {
				//getMainNode().setLocalTranslation(newPos);*/
				setWorldTranslation(newPos);
				/*return getMainNode();
			}
		});*/
	}


	public void scheduleNewRotation(SorcerersClient mainApp, final Quaternion newRot2) {
		// Don't need to schedule it since we're in the JME thread
		// Set rotation in Callable
		/*mainApp.enqueue(new Callable<Spatial>() {
			public Spatial call() throws Exception {*/
				getMainNode().setLocalRotation(newRot2);
	/*			return getMainNode();
			}
		});
*/
	}


	@Override
	public void remove() {
		if (rigidBodyControl != null) {
			this.module.getBulletAppState().getPhysicsSpace().remove(this.rigidBodyControl);
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


	/*public boolean canSee(PhysicalEntity cansee) {
		Ray r = new Ray(this.getMainNode().getWorldTranslation(), cansee.getMainNode().getWorldTranslation().subtract(this.getMainNode().getWorldTranslation()).normalizeLocal());
		//synchronized (module.objects) {
		//if (go.collides) {
		CollisionResults results = new CollisionResults();
		Iterator<IEntity> it = module.entities.iterator();
		while (it.hasNext()) {
			IEntity o = it.next();
			if (o instanceof PhysicalEntity && o != this) {
				PhysicalEntity go = (PhysicalEntity)o;
				// if (go.collides) {
				if (go.getMainNode().getWorldBound() != null) {
					results.clear();
					try {
						go.getMainNode().collideWith(r, results);
					} catch (UnsupportedCollisionException ex) {
						System.out.println("Spatial: " + go.getMainNode());
						ex.printStackTrace();
					}
					if (results.size() > 0) {
						float go_dist = this.distance(cansee)-1;
						CollisionResult cr = results.getClosestCollision();
						if (cr.getDistance() < go_dist) {
							return false;
						}
					}
				}
				//}
			}
		}
		return true;
	}*/


	/*public Vector3f getWorldTranslation() {
		//return this.main_node.getWorldTranslation(); 000?
		return this.rigidBodyControl.getPhysicsLocation();

	}*/


	public void setWorldTranslation(Vector3f pos) {
		// This is overridden by avatars, as they need to warp
		this.getMainNode().setLocalTranslation(pos.x, pos.y, pos.z);
	}


	public void applyForce(Vector3f dir) {
		rigidBodyControl.applyImpulse(dir, Vector3f.ZERO);//.applyCentralForce(dir);
	}


	public Vector3f getWorldTranslation() {
		return this.getMainNode().getLocalTranslation();
	}


	public Quaternion getRotation() {
		return this.getMainNode().getLocalRotation();
	}


	public boolean canMove() {
		return this.rigidBodyControl.getMass() > 0;// || this.rigidBodyControl.isKinematic();
	}


	public boolean hasMoved() {
		Vector3f newPos = this.getWorldTranslation();
		//boolean hasMoved = (newPos.x != this.prevPos.x || newPos.y != this.prevPos.y || newPos.y != this.prevPos.y); // todo - check rotation
		float dist = newPos.distance(prevPos);
		boolean hasMoved = dist > 0.001f; //!newPos.equals(prevPos); 
		if (hasMoved) {
			Settings.p(this.toString() + " has moved " + dist);
			this.prevPos.x = newPos.x;
			this.prevPos.y = newPos.y;
			this.prevPos.z = newPos.z;
		}

		return hasMoved;
	}


	@Override
	public String toString() {
		return super.toString();
	}


	public abstract HashMap<String, Object> getCreationData();

}
