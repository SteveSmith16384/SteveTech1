package com.scs.stevetech1.entities;

import java.util.HashMap;
import java.util.Iterator;

import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.scs.simplephysics.ISimpleEntity;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IAnimatedClientSide;
import com.scs.stevetech1.components.IPhysicalEntity;
import com.scs.stevetech1.components.ILaunchable;
import com.scs.stevetech1.components.IProcessByServer;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.components.ISetRotation;
import com.scs.stevetech1.netmessages.EntityUpdateData;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.server.RayCollisionData;
import com.scs.stevetech1.shared.ChronologicalLookup;
import com.scs.stevetech1.shared.EntityPositionData;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.shared.PositionCalculator;

public abstract class PhysicalEntity extends Entity implements IPhysicalEntity, IProcessByServer, ISimpleEntity<PhysicalEntity> {

	protected Node mainNode;
	public SimpleRigidBody<PhysicalEntity> simpleRigidBody;
	public PositionCalculator historicalPositionData; // Used client side for all entities (for position interpolation), and server side for Avatars, for rewinding position
	public ChronologicalLookup<EntityUpdateData> chronoUpdateData; // Used client-side for extra update data, e.g. current animation, current direction
	public boolean collideable = true;
	public boolean blocksView;

	// Rewind settings
	private Vector3f originalPos = new Vector3f();

	public boolean sendUpdate = true; // Send first time.  Don't forget to set to true if any data changes that is included in the EntityUpdateMessage
	public Node owner;

	public PhysicalEntity(IEntityController _game, int id, int type, String _name, boolean _requiresProcessing, boolean _blocksView) {
		super(_game, id, type, _name, _requiresProcessing);

		blocksView = _blocksView;

		historicalPositionData = new PositionCalculator(true, 100);
		mainNode = new Node(name + "_MainNode_" + id);

		if (!game.isServer()) {
			chronoUpdateData = new ChronologicalLookup<EntityUpdateData>(true, 100);
		}
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		if (this instanceof AbstractAvatar) {
			throw new RuntimeException("Do not call this for avatars!");
		}

		if (simpleRigidBody != null) {
			simpleRigidBody.process(tpf_secs);

			if (this.simpleRigidBody.movedByForces()) {
				if (getWorldTranslation().y < -1) {
					// Dropped away?
					Globals.p(getName() + " has fallen off the edge");
					fallenOffEdge();
				}
			}
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
		this.historicalPositionData.addPositionData(this.getWorldTranslation(), System.currentTimeMillis());
	}


	/*
	 * Called by the client 
	 */
	public void addPositionData(Vector3f pos, long time) {
		this.historicalPositionData.addPositionData(pos, time);	
	}


	// This is overridden by client avatars to take into account local position
	public void calcPosition(long serverTimeToUse, float tpf_secs) {
		EntityPositionData epd = historicalPositionData.calcPosition(serverTimeToUse, false);
		if (epd != null) {
			this.setWorldTranslation(epd.position);
			//this.setWorldRotation(epd.rotation);
		} else {
			//Settings.p("No position data for " + this);
		}

	}


	public void clearPositiondata() {
		this.historicalPositionData.clear();
	}


	@Override
	public void remove() {
		if (!removed) {
			if (simpleRigidBody != null) {
				if (Globals.STRICT) {
					if (this.game.getPhysicsController().containsSRB(simpleRigidBody) == false) {
						Globals.pe("Warning - srb is not in list for removal");
					}
				}
				this.game.getPhysicsController().removeSimpleRigidBody(simpleRigidBody);
				// simpleRigidBody = null;  Don't set it to null as it might be removed in mid-function
				if (Globals.STRICT) {
					if (this.game.getPhysicsController().getEntities().size() > game.getNumEntities()) {
						Globals.pe("Warning: more simple rigid bodies than entities!");
					}
				}
			}

			if (this.mainNode.getParent() != null) { // Unlaunched bullets have no parent
				this.mainNode.removeFromParent();
			}
		}
		super.remove();
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
		sendUpdate = true;
	}


	public void applyOneOffForce(Vector3f dir) {
		this.simpleRigidBody.getLinearVelocity().addLocal(dir);
	}


	public Quaternion getWorldRotation() {
		return this.getMainNode().getLocalRotation();
	}


	public void setWorldRotation(final Quaternion newRot2) {
		getMainNode().setLocalRotation(newRot2);
		this.sendUpdate = true;
	}


	public boolean sendUpdates() {
		return this.sendUpdate;
	}


	@Override
	public String toString() {
		return super.toString();
	}


	public void rewindPositionTo(long serverTimeToUse) {
		EntityPositionData shooterEPD = this.historicalPositionData.calcPosition(serverTimeToUse, true);
		if (shooterEPD != null) {
			this.originalPos.set(this.getWorldTranslation());
			//this.originalRot.set(this.getWorldRotation());
			this.setWorldTranslation(shooterEPD.position);
			//this.setWorldRotation(shooterEPD.rotation);
		} else {
			Globals.p("Unable to rewind position: no data");
		}
	}


	public void restorePosition() {
		this.setWorldTranslation(this.originalPos);
		//this.setWorldRotation(this.originalRot);
		this.mainNode.updateGeometricState();
	}


	public void adjustWorldTranslation(Vector3f offset) {
		this.setWorldTranslation(this.getWorldTranslation().add(offset));
	}


	public void fallenOffEdge() {
		// Override for avatars
		this.remove();
	}


	public RayCollisionData checkForRayCollisions(Ray r) {
		CollisionResults res = new CollisionResults();
		int c = game.getGameNode().collideWith(r, res);
		if (c == 0) {
			//Globals.p("No Ray collisions");
			return null;
		}
		Iterator<CollisionResult> it = res.iterator();
		while (it.hasNext()) {
			CollisionResult col = it.next();
			if (col.getDistance() > r.getLimit()) { // Keep this in! collideWith() seems to ignore it  
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
				if (pe != this) {
					if (game.canCollide(this, pe)) {
						/*if (pe != this && pe.collideable) {
					if (this instanceof IPlayerLaunchable) {
						if (pe instanceof IPlayerLaunchable) {
							continue; // Bullets don't collide with each other
						}
						IPlayerLaunchable bullet = (IPlayerLaunchable)this;
						if (bullet.getLauncher() == pe) {
							continue; // Don't collide with shooter
						}
					}*/
						//Settings.p("Ray collided with " + s + " at " + col.getContactPoint());
						return new RayCollisionData(pe, col.getContactPoint(), col.getDistance());
					}
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
		Vector3f ourPos = this.getMainNode().getWorldBound().getCenter();
		Vector3f theirPos = target.getMainNode().getWorldBound().getCenter();
		Ray r = new Ray(ourPos, theirPos.subtract(ourPos).normalizeLocal());
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
				//Globals.p("Ray collided with " + pe + " at " + col.getContactPoint());
				if (pe == this) {
					continue; // Don't block by outrselves
				}
				if (pe == target) {
					return true;
				}
				if (pe.blocksView) {
					return false;
				}
			}
		}

		return false;
	}


	public Node getOwnerNode() {
		return owner; // Override if the entity should have a different parent node to the default 
	}


	@Override
	public Collidable getCollidable() {
		return this.mainNode.getWorldBound(); // Return simple boundingbox by default, to avoid mesh v mesh collisions
		//return this.mainNode;
	}


	@Override
	public void moveEntity(Vector3f pos) {
		//this.getMainNode().move(pos); // Doesn't set sendUpdate flag!
		this.adjustWorldTranslation(pos);
	}


	/**
	 * Called client-side to store new position data sent by the server
	 * @param eum
	 * @param time
	 */
	public void storePositionData(EntityUpdateData eum, long time) {
		/*if (eum.force) {
			// Set it now!
			this.setWorldTranslation(eum.pos);
			this.clearPositiondata();
			/*if (pe == this.currentAvatar) {
				currentAvatar.clientAvatarPositionData.clear(); // Clear our local data as well
				currentAvatar.storeAvatarPosition(serverTime);
			}
		}*/
		this.addPositionData(eum.pos, time); // Store the position for use later
	}


	/**
	 * Called server-side to get a copy of the current data for updating the clients.
	 */
	public EntityUpdateData createEntityUpdateDataRecord() {
		EntityUpdateData updateData = new EntityUpdateData(this, System.currentTimeMillis());
		return updateData;
	}


	public void processChronoData(long serverTimeToUse, float tpf_secs) {
		EntityUpdateData epd = this.chronoUpdateData.get(serverTimeToUse, true);
		if (epd != null) {
			if (this instanceof ISetRotation) {
				ISetRotation isr = (ISetRotation)this;
				isr.setRotation(epd.aimDir);

			}
			if (this instanceof IAnimatedClientSide) {
				IAnimatedClientSide csa = (IAnimatedClientSide)this;
				csa.setAnimCode(epd.animationCode);
			}

		}

	}


}
