package com.scs.stevetech1.entities;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.components.IAnimatedServerSide;
import com.scs.stevetech1.components.IAvatarModel;
import com.scs.stevetech1.components.ICanScorePoints;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IGetReadyForGame;
import com.scs.stevetech1.components.IGetRotation;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.components.ITargetable;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.netmessages.AvatarStartedMessage;
import com.scs.stevetech1.netmessages.AvatarStatusMessage;
import com.scs.stevetech1.netmessages.EntityKilledMessage;
import com.scs.stevetech1.netmessages.EntityUpdateMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public abstract class AbstractServerAvatar extends AbstractAvatar implements IDamagable, IRewindable, IGetReadyForGame, ICanScorePoints, 
IGetRotation, IAnimatedServerSide, ITargetable {

	private AbstractGameServer server;
	public ClientData client;
	private float maxHealth;

	public AbstractServerAvatar(IEntityController _module, int avatarType, ClientData _client, IInputDevice _input, int eid, IAvatarModel anim, float _maxHealth, float _moveSpeed, float _jumpForce) {
		super(_module, avatarType, _client.getPlayerID(), _input, eid, _client.side, anim);

		server = (AbstractGameServer)_module;
		client = _client;
		maxHealth = _maxHealth;
		this.moveSpeed = _moveSpeed;
		this.setJumpForce(_jumpForce);
	}


	public void startAgain() {
		alive = true;
		//this.moveSpeed = server.getAvatarMoveSpeed(this); // todo - send to client if it changes
		//this.setJumpForce(server.getAvatarJumpForce(this)); // todo - send to client if it changes
		this.setHealth(maxHealth);
		this.simpleRigidBody.resetForces();//.currentGravInc = 0; // In case they fell off the edge
		this.invulnerableTimeSecs = 5;
		server.moveAvatarToStartPosition(this); // this also sends the update message to tell the client about the new move speed values etc...

		server.gameNetworkServer.sendMessageToAll(new AvatarStartedMessage(this));
		if (Globals.DEBUG_PLAYER_RESTART) {
			Globals.p("Sent AvatarStartedMessage");
		}

	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf) {
		if (!this.alive) { //this.getWorldTranslation()
			restartTimeSecs -= tpf;
			this.currentAnimCode = ANIM_DIED;
			if (this.restartTimeSecs <= 0) {
				Globals.p("Resurrecting avatar");
				this.startAgain();

				// Send position update
				EntityUpdateMessage eum = new EntityUpdateMessage();
				eum.addEntityData(this, true, this.createEntityUpdateDataRecord());
				server.gameNetworkServer.sendMessageToAll(eum);
			}
		} else {
			if (invulnerableTimeSecs >= 0) {
				invulnerableTimeSecs -= tpf;
			}

			super.serverAndClientProcess(server, null, tpf, System.currentTimeMillis());

			// Point us in the right direction
			//Vector3f lookAtPoint = this.getMainNode().getWorldTranslation().add(input.getDirection());// camLeft.add(camDir.mult(10));
			//lookAtPoint.y = this.getMainNode().getWorldTranslation().y; // Look horizontal!
			//this.getMainNode().lookAt(lookAtPoint, Vector3f.UNIT_Y); // need this in order to send the avatar's rotation to other players
			
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
	public void damaged(float amt, ICausesHarmOnContact collider, String reason) {
		if (server.getGameData().getGameStatus() == SimpleGameData.ST_STARTED) {
			if (this.alive && invulnerableTimeSecs < 0) {
				this.decHealth(amt);
				if (this.getHealth() <= 0) {
					IEntity killer = collider.getActualShooter();
					setDied(killer, reason);
				} else {
					Globals.p("Player " + this.getID() + " wounded " + amt + ": " + reason);
				}
			}
		}
	}


	private void setDied(IEntity killer, String reason) {
		Globals.p("Player " + this.getID() + " died: " + reason);
		this.alive = false;
		this.restartTimeSecs = server.gameOptions.avatarRestartTimeSecs;
		server.playerKilled(this);
		server.gameNetworkServer.sendMessageToAll(new EntityKilledMessage(this, killer));

		this.currentAnimCode = ANIM_DIED; // Send death as an anim, so it gets scheduled and is not shown straight away

		if (killer != null && killer instanceof ICanScorePoints) {
			ICanScorePoints csp = (ICanScorePoints)killer;
			csp.incScore(1);
		}
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
		// Don't call startAgain() since that moves the avatar
		//this.startAgain();
		
		alive = true;
		this.setHealth(maxHealth);
		this.client.setScore(0);
		this.invulnerableTimeSecs = 5;

	}



	@Override
	public void incScore(int i) {
		client.incScore(i);
		this.sendStatusUpdateMessage(false);
	}

	
	protected void sendStatusUpdateMessage(boolean damaged) {
		this.server.gameNetworkServer.sendMessageToClient(client, new AvatarStatusMessage(this, client, damaged));

	}


	@Override
	public void setHealth(float h) {
		super.setHealth(h);
		this.sendStatusUpdateMessage(false);
	}


	@Override
	public void decHealth(float h) {
		super.decHealth(h);
		this.sendStatusUpdateMessage(true);
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
	public boolean isValidTargetForSide(int shootersSide) {
		return shootersSide != this.side;
	}


}
