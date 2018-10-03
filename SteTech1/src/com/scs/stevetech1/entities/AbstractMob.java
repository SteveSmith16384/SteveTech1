package com.scs.stevetech1.entities;

import java.util.HashMap;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IAffectedByPhysics;
import com.scs.stevetech1.components.IAnimatedClientSide;
import com.scs.stevetech1.components.IAnimatedServerSide;
import com.scs.stevetech1.components.IAvatarModel;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.components.IDontCollideWithComrades;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IGetRotation;
import com.scs.stevetech1.components.IKillable;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.components.ISetRotation;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.netmessages.EntityKilledMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.server.IArtificialIntelligence;
import com.scs.stevetech1.shared.IEntityController;

public abstract class AbstractMob extends PhysicalEntity implements IAffectedByPhysics, IDamagable, INotifiedOfCollision,
IRewindable, IAnimatedClientSide, IAnimatedServerSide, IProcessByClient, IGetRotation, ISetRotation, IKillable, IDontCollideWithComrades {

	private IAvatarModel model; // Need this to animate the model
	private float health;
	public byte side;
	protected IArtificialIntelligence ai;
	private int serverSideCurrentAnimCode; // Server-side
	private long timeKilled;

	public AbstractMob(IEntityController _game, int id, int type, float x, float y, float z, byte _side, 
			IAvatarModel _model, String name, float _health) {
		super(_game, id, type, name, true, false, true);

		side = _side;
		model = _model; // Need it for dimensions for bb
		health = _health;

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("side", side);
		} else {
			game.getGameNode().attachChild(this.model.createAndGetModel());
			this.setAnimCode_ClientSide(AbstractAvatar.ANIM_IDLE);
		}

		// Create box for collisions
		Box box = new Box(model.getCollisionBoxSize().x/2, model.getCollisionBoxSize().y/2, model.getCollisionBoxSize().z/2);
		Geometry bbGeom = new Geometry("bbGeom_" + name, box);
		bbGeom.setLocalTranslation(0, box.getYExtent(), 0); // origin is centre!
		bbGeom.setCullHint(CullHint.Always); // Don't draw the collision box
		bbGeom.setUserData(Globals.ENTITY, this);

		this.mainNode.attachChild(bbGeom);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), game.isServer(), this);
		simpleRigidBody.canWalkUpSteps = true;
		simpleRigidBody.setBounciness(0);

	}


	@Override
	public HashMap<String, Object> getCreationData() {
		HashMap<String, Object> creationData = super.getCreationData();
		// Need this in case the soldier is dead, in which case they won't send any updates, meaning
		// they won't get sent an animation code.
		creationData.put("animcode", this.getCurrentAnimCode_ServerSide());
		return creationData;
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		if (health > 0) {
			if (server.getGameData().getGameStatus() == SimpleGameData.ST_STARTED) {
				if (ai != null) {
				ai.process(server, tpf_secs);
				this.serverSideCurrentAnimCode = ai.getAnimCode();
				}
			} else {
				this.simpleRigidBody.setAdditionalForce(Vector3f.ZERO); // Stop moving
				this.serverSideCurrentAnimCode = AbstractAvatar.ANIM_IDLE; // Game ended so we're not moving.
			}
		} else {
			this.simpleRigidBody.setAdditionalForce(Vector3f.ZERO); // Stop moving
			long diff = System.currentTimeMillis() - timeKilled;
			if (diff > 5000) { // Remove corpse after a time
				game.markForRemoval(this);
				return;
			}
		}

		super.processByServer(server, tpf_secs);
	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		// Set position and direction of avatar model, which doesn't get moved automatically
		this.model.getModel().setLocalTranslation(this.getWorldTranslation());
	}


	@Override
	public void fallenOffEdge() {
		game.markForRemoval(this);
	}


	@Override
	public void damaged(float amt, IEntity collider, String reason) {
		if (Globals.DEBUG_BULLET_HIT) {
			Globals.p(this + " damaged()");
		}
		if (health > 0) {
			this.health -= amt;
			if (health <= 0) {
				if (Globals.DEBUG_BULLET_HIT) {
					Globals.p(this + " killed");
				}
				AbstractGameServer server = (AbstractGameServer)game;
				server.sendMessageToInGameClients(new EntityKilledMessage(this, collider, reason));
				this.serverSideCurrentAnimCode = AbstractAvatar.ANIM_DIED;
				this.sendUpdate = true; // Send new anim code

				this.game.getPhysicsController().removeSimpleRigidBody(this.simpleRigidBody); // Prevent us colliding
				this.simpleRigidBody.setMovedByForces(false);

				this.collideable = false;
				this.timeKilled = System.currentTimeMillis();

				server.appendToGameLog(entityName + " killed");
			}
		}
	}


	@Override
	public void remove() {
		super.remove();

		if (model.getModel() != null) {
			this.model.getModel().removeFromParent();
		}
	}


	@Override
	public byte getSide() {
		return side;
	}


	@Override
	public void notifiedOfCollision(PhysicalEntity pe) {
		if (health > 0) {
			if (game.isServer()) {
				if (ai != null) {
					ai.collided(pe);
				}
			}
		}
	}


	@Override
	public void setAnimCode_ClientSide(int animCode) {
		if (model != null) {
			this.model.setAnim(animCode);
		}
	}


	@Override
	public void processManualAnimation_ClientSide(float tpf_secs) {
		// Do nothing, already handled
	}


	/**
	 * Called server-side only,
	 */
	@Override
	public int getCurrentAnimCode_ServerSide() {
		return this.serverSideCurrentAnimCode;
	}


	@Override
	public void setRotation(Vector3f dir) {
		Vector3f newdir = new Vector3f(dir.x, 0, dir.z); // Keep horizontal
		JMEAngleFunctions.rotateToWorldDirection(this.model.getModel(), newdir);
	}


	@Override
	public Vector3f getRotation() {
		return ai.getDirection();
	}


	@Override
	public void handleKilledOnClientSide(PhysicalEntity killer) {
		// Override if required
	}


	@Override
	public float getHealth() {
		return health;
	}


	@Override
	public boolean canBeDamaged() {
		return this.health > 0;
	}


	@Override
	public void updateClientSideHealth(int amt) {
	}

}
