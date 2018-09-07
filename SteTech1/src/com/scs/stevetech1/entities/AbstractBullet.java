package com.scs.stevetech1.entities;

import java.util.HashMap;

import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IDontCollideWithComrades;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.server.RayCollisionData;
import com.scs.stevetech1.shared.IEntityController;

public abstract class AbstractBullet extends PhysicalEntity implements IProcessByClient, ICausesHarmOnContact, IDontCollideWithComrades {

	public int playerID;
	public IEntity shooter; // So we know who not to collide with
	private byte side;

	private ClientData client; // Only used server-side
	protected Vector3f origin;

	protected boolean useRay; // Use ray for CCD?
	private Vector3f dir;
	protected float speed;
	private float range;

	public AbstractBullet(IEntityController _game, int entityId, int type, String name, int _playerOwnerId, IEntity _shooter, Vector3f startPos, Vector3f _dir, byte _side, ClientData _client, boolean _useRay, float _speed, float _range) {
		super(_game, entityId, type, name, true, false, true);

		playerID = _playerOwnerId;
		client = _client;
		useRay = _useRay;
		speed = _speed;
		range = _range;
		side = _side;
		shooter = _shooter;
		dir = _dir;

		if (Globals.STRICT) {
			if (side <= 0) {
				throw new RuntimeException("Invalid side: " + side);
			}
			if (!game.isServer() && this.getID() > 0) {
				throw new RuntimeException("Client bullets should have a negtive ID!");
			}
		}

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("side", side);
			creationData.put("playerID", playerID);
			creationData.put("shooterID", shooter.getID());
			creationData.put("startPos", startPos);
			creationData.put("dir", dir);
		}		
		//this.getMainNode().setUserData(Globals.ENTITY, this);

		origin = startPos.clone(); // todo - don't create each time

		this.createModelAndSimpleRigidBody(dir);

		this.setWorldTranslation(startPos);
		this.mainNode.updateGeometricState();

		if (game.isServer() && playerID > 0) { // Don't ffwd AI bullets
			if (Globals.DEBUG_DELAYED_EXPLOSION) {
				Globals.p("Start of ffwding -------------------------");
			}
			AbstractGameServer server = (AbstractGameServer)game;

			// fast forward it!
			float totalTimeToFFwd_Ms = server.gameOptions.clientRenderDelayMillis + (client.playerData.pingRTT/2);
			float tpf_secs = (float)server.gameOptions.tickrateMillis / 1000f;
			while (totalTimeToFFwd_Ms > 0) {
				totalTimeToFFwd_Ms -= server.gameOptions.tickrateMillis;
				this.processByServer(server, tpf_secs);
				if (this.removed) {
					if (Globals.DEBUG_DELAYED_EXPLOSION) {
						Globals.p("Bullet removed in mid ffwd");
					}
					break;
				}
			}

			if (Globals.DEBUG_DELAYED_EXPLOSION) {
				Globals.p("End of ffwding -------------------------");
			}

			// If server, send messages to clients to tell them it has been launched
			//LaunchData ld = new LaunchData(startPos, dir, shooter.getID(), System.currentTimeMillis() - server.gameOptions.clientRenderDelayMillis);
			//long launchTime = System.currentTimeMillis() - server.gameOptions.clientRenderDelayMillis; // "-Globals.CLIENT_RENDER_DELAY" so they render it immed.
			//EntityLaunchedMessage msg = new EntityLaunchedMessage(this.getID(), this.playerID, startPos, _dir, shooter.getID(), launchTime);
			//server.sendMessageToInGameClients(msg);
		}

		if (Globals.STRICT) {
			if (this.useRay && this.simpleRigidBody != null) {
				throw new RuntimeException("Bullet uses ray and SRB");
			}
		}

	}


	protected abstract void createModelAndSimpleRigidBody(Vector3f dir);


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		if (!useRay) {
			super.processByServer(server, tpf_secs);
		} else {
			this.moveByRay(tpf_secs);
		}
		/*
			Ray ray = new Ray(this.getWorldTranslation(), dir);
			ray.setLimit(speed * tpf_secs);
			RayCollisionData rcd = this.checkForRayCollisions(ray);
			if (rcd != null) {
				game.markForRemoval(this.getID());
				game.collisionOccurred(this, rcd.entityHit);
			} else {
				// Move spatial
				Vector3f offset = this.dir.mult(speed * tpf_secs);
				this.adjustWorldTranslation(offset);
				if (Globals.DEBUG_DELAYED_EXPLOSION) {
					Globals.p("Server," + System.currentTimeMillis() + ",Pos," + this.getWorldTranslation());
				}
			}
		}*/

		if (!this.markedForRemoval) {
			if (range > 0) {
				float dist = this.getDistanceTravelled();
				if (dist > range) {
					game.markForRemoval(this.getID());
				}
			}
		}
	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		if (!useRay) {
			simpleRigidBody.process(tpf_secs);
		} else {
			/*Ray ray = new Ray(this.getWorldTranslation(), dir);
			ray.setLimit(speed * tpf_secs);
			RayCollisionData rcd = this.checkForRayCollisions(ray);
			if (rcd != null) {
				game.markForRemoval(this.getID());
				// Since we're bypassing the physics engine, we need to handle collisions manually
				if (this instanceof INotifiedOfCollision) {
					INotifiedOfCollision inoc = (INotifiedOfCollision)this;
					inoc.collided(rcd.entityHit);
				}
			} else {
				// Move spatial
				Vector3f offset = this.dir.mult(speed * tpf_secs);
				this.adjustWorldTranslation(offset);
				if (Globals.DEBUG_DELAYED_EXPLOSION) {
					Globals.p("Client," + System.currentTimeMillis() + ",Pos," + this.getWorldTranslation());
				}
			}*/
			this.moveByRay(tpf_secs);
		}
		if (!this.markedForRemoval) {
			if (range > 0) {
				float dist = this.origin.distance(this.getWorldTranslation());
				if (dist > range) {
					game.markForRemoval(this.getID());
				}
			}
		}
	}


	private void moveByRay(float tpf_secs) {
		Ray ray = new Ray(this.getWorldTranslation(), dir);
		ray.setLimit(speed * tpf_secs);
		RayCollisionData rcd = this.checkForRayCollisions(ray);
		if (rcd != null) {
			game.markForRemoval(this.getID());
			game.collisionOccurred(this, rcd.entityHit);
		} else {
			// Move spatial
			Vector3f offset = this.dir.mult(speed * tpf_secs);
			this.adjustWorldTranslation(offset);
			if (Globals.DEBUG_DELAYED_EXPLOSION) {
				Globals.p("Server," + System.currentTimeMillis() + ",Pos," + this.getWorldTranslation());
			}
		}
	}


	public float getDistanceTravelled() {
		return this.origin.distance(this.getWorldTranslation());
	}


	@Override
	public void calcPosition(long serverTimeToUse, float tpf_secs) {
		// Do nothing!  The client controls it.
	}


	@Override
	public void processChronoData(long serverTimeToUse, float tpf_secs) {
		// Do nothing, each client controls the position
	}


	@Override
	public boolean sendUpdates() {
		return false; // No, each client controls the position
	}



	@Override
	public byte getSide() {
		return side;
	}


	@Override
	public IEntity getActualShooter() {
		return shooter;
	}

	/*
	@Override
	public float getDamageCaused() {
		//return ((RANGE-this.getDistanceTravelled()) / this.getDistanceTravelled()) * 10;
		float dam = (((range-this.getDistanceTravelled()) / this.getDistanceTravelled()) * 5)+5;
		Globals.p(this + " damage: " + dam);
		return dam;
	}
	 */
}

