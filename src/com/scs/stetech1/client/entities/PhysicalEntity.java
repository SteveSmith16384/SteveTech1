package com.scs.stetech1.client.entities;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.scs.stetech1.components.IProcessable;
import com.scs.stetech1.components.ISharedEntity;
import com.scs.stetech1.shared.IEntityController;

public abstract class PhysicalEntity extends Entity implements IProcessable, ISharedEntity {

	protected Node main_node;
	public RigidBodyControl rigidBodyControl;

	public PhysicalEntity(IEntityController _game, int type, String _name) {
		super(_game, type, _name);

		main_node = new Node(name + "_MainNode");
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


	public Vector3f getLocation() {
		//return this.main_node.getWorldTranslation(); 000?
		return this.rigidBodyControl.getPhysicsLocation();
		
	}


	public void applyForce(Vector3f dir) {
		rigidBodyControl.applyImpulse(dir, Vector3f.ZERO);//.applyCentralForce(dir);
	}


	@Override
	public Vector3f getLocalTranslation() {
		return this.getMainNode().getLocalTranslation();
	}


	@Override
	public Quaternion getRotation() {
		return this.getMainNode().getLocalRotation();
	}


	@Override
	public boolean canMove() {
		return this.rigidBodyControl.getMass() > 0;
	}

}
