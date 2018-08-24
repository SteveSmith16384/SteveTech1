package com.scs.stevetech1.entities;

import java.util.HashMap;

import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IBullet;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.components.ILaunchable;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.netmessages.EntityLaunchedMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.server.RayCollisionData;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.systems.client.LaunchData;

/**
 * These bullets are processed separately on the client and the server, but includes code to ensure they are ready to be fired immediately, and
 * launch at (almost) the same time on the client and the server. 
 *
 */
public abstract class AbstractPlayersBullet extends PhysicalEntity implements IProcessByClient, ILaunchable, ICausesHarmOnContact, IBullet {

	protected boolean launched = false;
	public int playerID;
	public IEntity shooter; // So we know who not to collide with
	private int side;
	private IEntityContainer<AbstractPlayersBullet> container;

	private ClientData client; // Only used server-side
	protected Vector3f origin;

	protected boolean useRay;
	private Vector3f dir;
	protected float speed;
	private float range;

	public AbstractPlayersBullet(IEntityController _game, int entityId, int type, String name, int _playerOwnerId, IEntityContainer<AbstractPlayersBullet> _container, int _side, ClientData _client, Vector3f _dir, boolean _useRay, float _speed, float _range) {
		super(_game, entityId, type, name, true, false, true);

		playerID = _playerOwnerId;
		container = _container;
		client = _client;
		dir = _dir;
		useRay = _useRay;
		speed = _speed;
		range = _range;
		side = _side;

		if (Globals.STRICT) {
			if (side <= 0) {
				throw new RuntimeException("Invalid side: " + side);
			}
		}

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("side", side);
			creationData.put("playerID", playerID);
			creationData.put("containerID", container.getID());
		}

		if (container != null) { // Only snowball fired by us have a container
			container.addToCache(this);
		}

		this.collideable = false; // Wait until launched

	}


	@Override
	public void launch(IEntity _shooter, Vector3f startPos, Vector3f _dir) {
		if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
			Globals.p("Launching entity " + this);
		}

		if (Globals.STRICT) {
			if (launched) {
				throw new RuntimeException("Trying to relaunch launched bullet");
			}
			if (this.getMainNode().getChildren().size() > 0) {
				throw new RuntimeException("Trying to relaunch launched bullet (has children)");
			}
			if (_shooter == null) {
				throw new RuntimeException("Null launcher");
			}
		}

		launched = true;
		dir = _dir;
		shooter = _shooter;
		origin = startPos.clone();

		this.createModelAndSimpleRigidBody(dir);

		game.getGameNode().attachChild(this.mainNode); // Need this for client side
		this.setWorldTranslation(startPos);
		this.mainNode.updateGeometricState();

		this.collideable = true;

		if (game.isServer()) {
			if (Globals.DEBUG_DELAYED_EXPLOSION) {
				Globals.p("Start of ffwding -------------------------");
			}
			AbstractGameServer server = (AbstractGameServer)game;

			// fast forward it!
			float totalTimeToFFwd_Ms = server.clientRenderDelayMillis + (client.playerData.pingRTT/2);
			//float totalTimeToFFwd_Ms = server.clientRenderDelayMillis + (client.playerData.pingRTT*40);
			float tpf_secs = (float)server.tickrateMillis / 1000f;
			while (totalTimeToFFwd_Ms > 0) {
				totalTimeToFFwd_Ms -= server.tickrateMillis;
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
			LaunchData ld = new LaunchData(startPos, dir, shooter.getID(), System.currentTimeMillis() - server.clientRenderDelayMillis); // "-Globals.CLIENT_RENDER_DELAY" so they render it immed.
			server.sendMessageToInGameClients(new EntityLaunchedMessage(this.getID(), this.playerID, ld));
		}

	}


	protected abstract void createModelAndSimpleRigidBody(Vector3f dir);


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		if (launched) {
			if (!useRay) {
				super.processByServer(server, tpf_secs);
			} else {
				Ray ray = new Ray(this.getWorldTranslation(), dir);
				ray.setLimit(speed * tpf_secs);
				RayCollisionData rcd = this.checkForRayCollisions(ray);
				if (rcd != null) {
					this.remove();
					server.collisionOccurred(this, rcd.entityHit);
				} else {
					// Move spatial
					Vector3f offset = this.dir.mult(speed * tpf_secs);
					this.adjustWorldTranslation(offset);
					if (Globals.DEBUG_DELAYED_EXPLOSION) {
						Globals.p("Server," + System.currentTimeMillis() + ",Pos," + this.getWorldTranslation());
					}
				}
			}

			if (range > 0) {
				float dist = this.getDistanceTravelled();
				if (dist > range) {
					this.remove();
				}
			}
		}
	}


	public float getDistanceTravelled() {
		return this.origin.distance(this.getWorldTranslation());
	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		if (this.launched) {
			if (!useRay) {
				simpleRigidBody.process(tpf_secs);
			} else {
				Ray ray = new Ray(this.getWorldTranslation(), dir);
				ray.setLimit(speed * tpf_secs);
				RayCollisionData rcd = this.checkForRayCollisions(ray);
				if (rcd != null) {
					this.remove();
				} else {
					// Move spatial
					Vector3f offset = this.dir.mult(speed * tpf_secs);
					this.adjustWorldTranslation(offset);
					if (Globals.DEBUG_DELAYED_EXPLOSION) {
						Globals.p("Client," + System.currentTimeMillis() + ",Pos," + this.getWorldTranslation());
					}
				}
			}

			if (range > 0) {
				float dist = this.origin.distance(this.getWorldTranslation());
				if (dist > range) {
					this.remove();
				}
			}
		}
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
	public IEntity getLauncher() {
		return shooter;
	}


	@Override
	public int getSide() {
		return side;
	}

/*
	@Override
	public boolean isClientControlled() {
		return launched; // All launched bullets are under client control
	}
*/

	@Override
	public boolean hasBeenLaunched() {
		return this.launched;
	}


	@Override
	public IEntity getActualShooter() {
		return shooter;
	}


	@Override
	public float getDamageCaused() {
		//return ((RANGE-this.getDistanceTravelled()) / this.getDistanceTravelled()) * 10;
		float dam = (((range-this.getDistanceTravelled()) / this.getDistanceTravelled()) * 5)+5;
		Globals.p(this + " damage: " + dam);
		return dam;
	}


	@Override
	public void remove() {
		this.container.removeFromCache(this);
		super.remove();
	}


}

