package com.scs.stevetech1.entities;

import java.util.HashMap;

import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IAddedImmediately;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IDontCollideWithComrades;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.server.RayCollisionData;
import com.scs.stevetech1.shared.IEntityController;

public abstract class AbstractBullet extends PhysicalEntity implements IProcessByClient, ICausesHarmOnContact, IDontCollideWithComrades, IAddedImmediately, IRewindable {

	public int playerID; // -1 if AI
	public IEntity shooter; // So we know who not to collide with, and who fired the killing shot
	private byte side;

	private ClientData client; // Only used server-side
	protected Vector3f origin = new Vector3f();

	protected boolean useRay; // Use ray for CCD?
	private Vector3f dir;
	protected float speed;
	private float range;
	//protected int serverEntityId; // So the client can tell the server which entity it is
	
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
		//serverEntityId = _serverEntityId;
		
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

		origin.set(startPos);

		this.createModelAndSimpleRigidBody(dir);

		if (Globals.STRICT) {			
			if (this.simpleRigidBody == null && speed <= 0) {
				throw new RuntimeException("Invalid speed: " + speed);
			}
		}
		
		this.setWorldTranslation(startPos);
		this.mainNode.updateGeometricState();

		if (game.isServer() && isPlayersBullet()) { // Don't ffwd AI bullets
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
				if (this.markedForRemoval || this.removed) {
					if (Globals.DEBUG_DELAYED_EXPLOSION) {
						Globals.p("Bullet removed in mid ffwd");
					}
					break;
				}
			}

			if (Globals.DEBUG_DELAYED_EXPLOSION) {
				Globals.p("End of ffwding -------------------------");
			}

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
		this.finalProcessing();
	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		if (!useRay) {
			simpleRigidBody.process(tpf_secs);
		} else {
			this.moveByRay(tpf_secs);
		}
		this.finalProcessing();
	}
	
	
	private void moveByRay(float tpf_secs) {
		Ray ray = new Ray(this.getWorldTranslation(), dir);
		ray.setLimit(speed * tpf_secs);
		RayCollisionData rcd = this.checkForRayCollisions(ray);
		if (rcd != null) {
			game.collisionOccurred(this, rcd.entityHit);
			game.markForRemoval(this);
		} else {
			// Move spatial
			Vector3f offset = this.dir.mult(speed * tpf_secs);
			this.adjustWorldTranslation(offset);
			if (Globals.DEBUG_DELAYED_EXPLOSION) {
				Globals.p("Server," + System.currentTimeMillis() + ",Pos," + this.getWorldTranslation());
			}
		}
	}


	private void finalProcessing() {
		if (!this.markedForRemoval) {
			if (Globals.DEBUG_BULLET_POSITIONS) {
				Vector3f pos = this.getWorldTranslation();
				DebuggingSphere ds = new DebuggingSphere(game, game.getNextEntityID(), pos.x, pos.y, pos.z, false, true);
				game.addEntity(ds);
			}
			if (range > 0) {
				float dist = this.origin.distance(this.getWorldTranslation());
				if (dist > range) {
					game.markForRemoval(this);
				}
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


	@Override
	public boolean shouldClientAddItImmediately() {
		return isPlayersBullet();
	}


	public boolean isPlayersBullet() {
		return this.playerID >= 0;
	}
}

