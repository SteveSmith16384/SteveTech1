package com.scs.stetech1.client.entities;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.scs.stetech1.client.SorcerersClient;
import com.scs.stetech1.components.IProcessable;
import com.scs.stetech1.components.ISharedEntity;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityPositionData;
import com.scs.stetech1.shared.IEntityController;

public abstract class PhysicalEntity extends Entity implements IProcessable, ISharedEntity {

	protected Node main_node;
	public RigidBodyControl rigidBodyControl;
	private ArrayList<EntityPositionData> positionData = new ArrayList<>(); // todo - use diff type of list


	public PhysicalEntity(IEntityController _game, int type, String _name) {
		super(_game, type, _name);

		main_node = new Node(name + "_MainNode");
	}


	public void clearPositiondata() {
		synchronized (positionData) {
			positionData.clear();
		}
	}
	
	
	public void addPositionData(EntityPositionData newData) {
		synchronized (positionData) {
			for(int i=0 ; i<this.positionData.size() ; i++) {
				// Time gets earlier
				EntityPositionData epd = this.positionData.get(i);
				if (epd.serverStateTime < newData.serverStateTime) {
					positionData.add(i, newData); // insert at position based on timestamp!
					// Remove later entries
					while (this.positionData.size() > i+3) {
						this.positionData.remove(i+1);
					}
					return;
				}
			}
			// Add to end
			positionData.add(newData); // insert at position based on timestamp!
		}
	}


	public void calcPosition(SorcerersClient mainApp, long serverTime) {
		synchronized (positionData) {
			if (this.positionData.size() > 1) {
				EntityPositionData firstEPD = null;
				for(EntityPositionData secondEPD : this.positionData) {
					// Time gets earlier
					if (firstEPD == null) {
						firstEPD = secondEPD;
						if (firstEPD.serverStateTime < serverTime) {
							return; // To early!
						}
					} else if (firstEPD.serverStateTime > serverTime && secondEPD.serverStateTime < serverTime) {
						// interpol between positions
						float frac = (firstEPD.serverStateTime - serverTime) / (serverTime - secondEPD.serverStateTime);
						final Vector3f newPos = firstEPD.position.interpolate(secondEPD.position, frac);
						// Set positions in Callable
						{
							/*mainApp.enqueue(new Callable<Spatial>() {
								public Spatial call() throws Exception {
									getMainNode().setLocalTranslation(newPos);
									return getMainNode();
								}
							})*/;
							//this.getMainNode().setLocalTranslation(newPos);
							this.setPosition(mainApp, newPos);
						}
						if (module.getPlayersAvatar() != this) { // if its our avatar, don't adjust rotation!
							Quaternion newRot = new Quaternion();
							final Quaternion newRot2 = newRot.slerp(firstEPD.rotation, secondEPD.rotation, frac);
							//this.getMainNode().setLocalRotation(newRot);
							{
								/*// Set rotation in Callable
								mainApp.enqueue(new Callable<Spatial>() {
									public Spatial call() throws Exception {
										getMainNode().setLocalRotation(newRot2);
										return getMainNode();
									}
								});*/
								this.setRotation(mainApp, newRot2);
							}
						} else {
							Settings.p("Updated avatar pos: " + newPos);
						}
						return;
					}
				}
				// If we got this far, all position data is too old!
			}
		}

	}


	public void setPosition(SorcerersClient mainApp, final Vector3f newPos) {
		mainApp.enqueue(new Callable<Spatial>() {
			public Spatial call() throws Exception {
				getMainNode().setLocalTranslation(newPos);
				return getMainNode();
			}
		});
	}
	
	
	public void setRotation(SorcerersClient mainApp, final Quaternion newRot2) {
		// Set rotation in Callable
		mainApp.enqueue(new Callable<Spatial>() {
			public Spatial call() throws Exception {
				getMainNode().setLocalRotation(newRot2);
				return getMainNode();
			}
		});

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
