package com.scs.stevetech1.entities;

import java.util.HashMap;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.avatartypes.IAvatarControl;
import com.scs.stevetech1.components.IAnimatedServerSide;
import com.scs.stevetech1.components.IAvatarModel;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IGetReadyForGame;
import com.scs.stevetech1.components.IGetRotation;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.components.ITargetableByAI;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.netmessages.AvatarStartedMessage;
import com.scs.stevetech1.netmessages.AvatarStatusMessage;
import com.scs.stevetech1.netmessages.EntityKilledMessage;
import com.scs.stevetech1.netmessages.EntityUpdateMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public abstract class AbstractServerAvatar extends AbstractAvatar implements IDamagable, IRewindable, IGetReadyForGame,  
IGetRotation, IAnimatedServerSide, ITargetableByAI {

	private AbstractGameServer server;
	public ClientData client;
	private float maxHealth;
	private int playerTargetPriority;

	public AbstractServerAvatar(IEntityController _module, int avatarType, ClientData _client, IInputDevice _input, int eid, IAvatarModel anim, 
			float _maxHealth, int _playerTargetPriority, IAvatarControl _avatarControl) {
		super(_module, avatarType, _client.getPlayerID(), _input, eid, _client.getSide(), anim, _avatarControl);

		if (game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("id", eid); this.getID();
			creationData.put("playerID", _client.getPlayerID());
			creationData.put("side", side);
			creationData.put("playersName", _client.playerData.playerName);
		}

		server = (AbstractGameServer)_module;
		client = _client;
		maxHealth = _maxHealth;
		playerTargetPriority = _playerTargetPriority;
	}


	public void startAgain() {
		this.setAlive(true);
		this.setHealth(maxHealth);
		this.simpleRigidBody.resetForces(); // In case they fell off the edge, stop them falling after restarting
		this.invulnerableTimeSecs = 5; // todo - config
		server.moveAvatarToStartPosition(this);

		server.sendMessageToInGameClients(new AvatarStartedMessage(this));
		this.sendAvatarStatusUpdateMessage(false);
		if (Globals.DEBUG_PLAYER_RESTART) {
			Globals.p("Sent AvatarStartedMessage");
		}

	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf) {
		if (!this.alive) {
			restartTimeSecs -= tpf;
			this.currentAnimCode = ANIM_DIED;
			if (this.restartTimeSecs <= 0) {
				Globals.p("Respawning avatar");
				this.startAgain();

				// Send position update
				EntityUpdateMessage eum = new EntityUpdateMessage();
				eum.addEntityData(this, true, this.createEntityUpdateDataRecord());
				server.sendMessageToInGameClients(eum);
			}
		} else {
			if (invulnerableTimeSecs >= 0) {
				invulnerableTimeSecs -= tpf;
			}

			super.serverAndClientProcess(server, null, tpf, System.currentTimeMillis());

			if (getWorldTranslation().y < -1) {
				// Dropped off the edge?
				//server.console.appendText(getName() + " has fallen off the edge");
				fallenOffEdge();
			}

			if (this instanceof IRewindable) {
				addPositionData();
			}
		}
	}


	@Override
	public void damaged(float amt, IEntity collider, String reason) {
		if (server.getGameData().getGameStatus() == SimpleGameData.ST_STARTED) {
			if (this.alive && invulnerableTimeSecs < 0 && this.getHealth() > 0) {
				this.decHealth(amt);
				if (this.getHealth() <= 0) {
					IEntity killer = collider;
					if (collider instanceof ICausesHarmOnContact) {
						ICausesHarmOnContact choc = (ICausesHarmOnContact)collider;
						killer = choc.getActualShooter();
						if (killer == null) {
							killer = collider;
						}
					}
					setDied(killer, reason);
				} else {
					Globals.p("Player " + this.getID() + " wounded " + amt + ": " + reason);
				}
			}
		}
	}


	protected void setDied(IEntity killer, String reason) {
		Globals.p("Player " + this.getID() + " died: " + reason);
		this.setAlive(false);
		this.restartTimeSecs = server.gameOptions.avatarRestartTimeSecs;
		server.playerKilled(this);
		server.sendMessageToInGameClients(new EntityKilledMessage(this, killer, reason));

		this.currentAnimCode = ANIM_DIED; // Send death as an anim, so it gets scheduled and is not shown straight away
	}


	@Override
	public Vector3f getShootDir() {
		return input.getDirection();
	}


	@Override
	public void fallenOffEdge() {
		if (this.alive) {
			Globals.p("playerID " + this.playerID + " has died due to falling off the edge (pos " + this.getWorldTranslation() + ")");
			setDied(null, "fallen Off Edge");
		}
	}


	@Override
	public void getReadyForGame() {
		//this.startAgain();  Don't call startAgain() since that moves the avatar

		this.setAlive(true);
		this.setHealth(maxHealth);
		this.invulnerableTimeSecs = 5;

	}


	protected void sendAvatarStatusUpdateMessage(boolean damaged) {
		this.server.gameNetworkServer.sendMessageToClient(client, new AvatarStatusMessage(this, client, damaged));

	}


	@Override
	public void setHealth(float h) {
		if (this.getHealth() != h) {
			super.setHealth(h);
			this.sendAvatarStatusUpdateMessage(false);
		}
	}


	public float getMaxHealth() {
		return this.maxHealth;
	}


	@Override
	public void decHealth(float h) {
		super.decHealth(h);
		this.sendAvatarStatusUpdateMessage(true);
	}


	@Override
	public Vector3f getRotation() {
		return input.getDirection();
	}


	@Override
	public int getCurrentAnimCode() {
		return this.currentAnimCode;
	}



	@Override
	public boolean isValidTargetForSide(byte shootersSide) {
		return shootersSide != this.side;
	}


	@Override
	public int getTargetPriority() {
		return playerTargetPriority;
	}


}
