package com.scs.stevetech1.entities;

import java.util.HashMap;
import java.util.Iterator;

import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Camera.FrustumIntersect;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.scs.simplephysics.ISimpleEntity;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IAnimatedClientSide;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.components.IDrawOnHUD;
import com.scs.stevetech1.components.IPhysicalEntity;
import com.scs.stevetech1.components.IProcessByServer;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.components.ISetRotation;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.netmessages.EntityUpdateData;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.server.RayCollisionData;
import com.scs.stevetech1.shared.ChronologicalLookup;
import com.scs.stevetech1.shared.EntityPositionData;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.shared.PositionCalculator;

public abstract class PhysicalEntity extends Entity implements IPhysicalEntity, IProcessByServer, ISimpleEntity<PhysicalEntity> {

	protected Node mainNode; // All spatials/models hang off the mainNode
	public SimpleRigidBody<PhysicalEntity> simpleRigidBody;
	public PositionCalculator historicalPositionData; // Used client side for all entities (for position interpolation), and server side for Avatars, for rewinding position
	public ChronologicalLookup<EntityUpdateData> chronoUpdateData; // Used client-side for extra update data, e.g. current animation, current direction

	public boolean collideable = true; // Primarily used for ray checks, since that doesn't use the physics engine
	public boolean blocksView; // Primarily used for canSee() ray checks, since that doesn't use the physics engine
	public long timeToAdd; // Client side only; when to add the entity to the game

	private Vector3f tmpRayPos = new Vector3f();

	public boolean sendUpdate = true; // Send first time.  Don't forget to set to true if any data changes that is included in the EntityUpdateMessage
	public boolean moves;
	private boolean isRewound = false; // Prevent storing historical positions when we've rewound the position

	public PhysicalEntity(IEntityController _game, int id, int type, String _name, boolean _requiresProcessing, boolean _blocksView, boolean _moves) {
		super(_game, id, type, _name, _requiresProcessing);

		blocksView = _blocksView;
		moves = _moves;

		mainNode = new Node(entityName + "_MainNode_" + id);
		this.getMainNode().setUserData(Globals.ENTITY, this);

		if (moves) {
			historicalPositionData = new PositionCalculator(Globals.HISTORY_DURATION_MILLIS, this.getName());
		}
		// Always create this (e.g. even Computer's need a health history)
		chronoUpdateData = new ChronologicalLookup<EntityUpdateData>(Globals.HISTORY_DURATION_MILLIS);
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpfSecs) {
		if (isRewound) {
			throw new RuntimeException("Trying to process rewound object: " + this);
		}

		if (this instanceof AbstractAvatar) {
			throw new RuntimeException("Do not call this for avatars!");
		}

		/*if (Globals.STRICT) {
			if (this.getMainNode().getParent() == null) {
				throw new RuntimeException("processing entity but not added to rootNode!");
			}
			if (!server.entities.containsKey(this.getID())) {
				throw new RuntimeException("processing entity but not added to game!");
			}
		}*/

		if (simpleRigidBody != null) {
			simpleRigidBody.process(tpfSecs);

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
	public void addPositionData() {
		if (!this.isRewound) {
			// Store the position for use when rewinding.
			this.addPositionData(System.currentTimeMillis());
		}
	}


	/*
	 * Called by the server 
	 */
	protected void addPositionData(long time) {
		if (!this.isRewound) {
			// Store the position for use when rewinding.
			this.historicalPositionData.addPositionData(this.getWorldTranslation(), time);
		}
	}


	/*
	 * Called by the client 
	 */
	public void addPositionData(Vector3f pos, long time) {
		if (!this.isRewound) {
			if (historicalPositionData != null) {
				this.historicalPositionData.addPositionData(pos, time);
			}
		}
	}


	// This is overridden by client avatars to take into account local position
	public void calcPosition(long serverTimeToUse, float tpf_secs) {
		if (historicalPositionData != null) {
			EntityPositionData epd = historicalPositionData.calcPosition(serverTimeToUse, false);
			if (epd != null) {
				this.setWorldTranslation(epd.position);
			} else {
				//Settings.p("No position data for " + this);
			}
		}
	}


	public void clearPositiondata() {
		this.historicalPositionData.clear();
	}


	@Override
	public void remove() {
		if (!removed) {
			if (simpleRigidBody != null) {
				this.game.getPhysicsController().removeSimpleRigidBody(simpleRigidBody);
				// simpleRigidBody = null;  Don't set it to null as it might be removed in mid-function
				if (Globals.STRICT) {
					if (this.game.getPhysicsController().getNumEntities() > game.getNumEntities()) {
						Globals.pe("Warning: more simple rigid bodies than entities!");
					}
				}
			}

			if (this.mainNode.getParent() != null) {
				this.mainNode.removeFromParent();
			}

			if (this instanceof IDrawOnHUD) {
				IDrawOnHUD idoh = (IDrawOnHUD)this;
				if (idoh.getHUDItem() != null) {
					idoh.getHUDItem().removeFromParent();
				}

			}
		}
		super.remove();
	}


	public Node getMainNode() {
		return mainNode;
	}


	public float distance(PhysicalEntity o) {
		return distance(o.getWorldTranslation());
	}


	public float distance(Vector3f pos) {
		float dist = this.getWorldTranslation().distance(pos);
		return dist;
	}


	public Vector3f getWorldTranslation() {
		return this.getMainNode().getLocalTranslation();
	}


	public void setWorldTranslation(Vector3f pos) {
		this.setWorldTranslation(pos.x, pos.y, pos.z);
	}


	public void setWorldTranslation(float x, float z) {
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


	public void rewindPositionTo(long serverTimeToUse) {
		EntityPositionData shooterEPD = this.historicalPositionData.calcPosition(serverTimeToUse, true);
		if (shooterEPD != null) {
			this.setWorldTranslation(shooterEPD.position);
			this.isRewound = true;
		} else {
			Globals.p("Unable to rewind position: no data");
		}
	}


	public void restorePosition() {
		if (this.isRewound) {
			this.rewindPositionTo(System.currentTimeMillis());
			this.mainNode.updateGeometricState();
			this.isRewound = false;
		}
	}


	public void adjustWorldTranslation(Vector3f offset) {
		if (!this.moves) {
			Globals.p("Warning: moving an entity that is marked as not moving!");
		}
		this.setWorldTranslation(this.getWorldTranslation().add(offset));
	}


	public void fallenOffEdge() {
		// Override for avatars
		game.markForRemoval(this);
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


	/**
	 * 
	 * @param target
	 * @param range
	 * @param maxAngleRads If negative, don't check the angle
	 * @return
	 */
	public boolean canSee(PhysicalEntity target, float range, float maxAngleRads) {
		Vector3f theirPos = target.getMainNode().getWorldBound().getCenter();
		// Check angle?
		if (maxAngleRads > 0) {
			float angle = JMEAngleFunctions.getAngleBetween(this.getMainNode(), theirPos);
			/*if (Globals.DEBUG_VIEW_ANGLE) {
				Globals.p(target + " and " + this + " is " + angle);
			}*/
			if (angle > maxAngleRads) {
				return false;
			}
		}

		// Note: Test the ray from the middle of the entity
		Vector3f ourPos = this.getMainNode().getWorldBound().getCenter();

		if (theirPos.distance(ourPos) > range) {
			return false;
		}

		Ray r = new Ray(ourPos, theirPos.subtract(ourPos, tmpRayPos).normalizeLocal());
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
					continue; // Don't block by ourselves
				}
				if (pe == target) {
					return true;
				}
				if (pe.blocksView) {
					return false;
				}
			}
		}

		return true;
	}


	@Override
	public Collidable getCollidable() {
		this.mainNode.updateModelBound(); // Need this!
		if (this.mainNode.getWorldBound().getVolume() == 0) {
			Globals.pe("Warning: " + this + " has zero volume!  Probably not addded to rootNode");
		}
		return this.mainNode.getWorldBound(); // Return simple boundingbox by default, to avoid mesh v mesh collisions
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
	public void addPositionData(EntityUpdateData eum, long time) {
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
		if (chronoUpdateData != null) {
			EntityUpdateData epd = this.chronoUpdateData.get(serverTimeToUse, true);
			if (epd != null) {
				if (this instanceof ISetRotation) {
					ISetRotation isr = (ISetRotation)this;
					isr.setRotation(epd.aimDir);

				}
				if (this instanceof IAnimatedClientSide) {
					IAnimatedClientSide csa = (IAnimatedClientSide)this;
					csa.setAnimCode_ClientSide(epd.animationCode);
				}
				if (this instanceof IDamagable) {
					IDamagable id = (IDamagable)this;
					id.updateClientSideHealth(epd.health);

				}

			}
		}
	}


	private Vector3f tmpHudPos = new Vector3f();
	private Vector3f tmpScreenPos = new Vector3f();

	protected void checkHUDNode(Node hudParent, Node hudItem, Camera cam, float maxDist, float yOffset) {
		boolean show = this.getWorldTranslation().distance(cam.getLocation()) < maxDist;
		show = show && cam.contains(this.getMainNode().getWorldBound()) != FrustumIntersect.Outside;
		if (show) {
			if (hudItem.getParent() == null) {
				hudParent.attachChild(hudItem);
			}
			tmpHudPos.set(this.getWorldTranslation());
			tmpHudPos.y += yOffset;
			Vector3f screen_pos = cam.getScreenCoordinates(tmpHudPos, tmpScreenPos);
			hudItem.setLocalTranslation(screen_pos.x, screen_pos.y, 0);
		} else {
			if (hudItem.getParent() != null) {
				hudItem.removeFromParent();
			}
		}
	}
	
	
	public SimpleRigidBody<PhysicalEntity> GetSimpleRigidBody() {
		return this.simpleRigidBody;
	}

}
