package com.scs.stevetech1.entities;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.scs.stevetech1.components.IAvatarModel;
import com.scs.stevetech1.components.ICanScorePoints;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IGetReadyForGame;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.netmessages.AvatarStartedMessage;
import com.scs.stevetech1.netmessages.AvatarStatusMessage;
import com.scs.stevetech1.netmessages.EntityKilledMessage;
import com.scs.stevetech1.netmessages.EntityUpdateMessage;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public abstract class AbstractServerAvatar extends AbstractAvatar implements IDamagable, IRewindable, IGetReadyForGame, ICanScorePoints {

	private AbstractGameServer server;
	public ClientData client;
	private Spatial dummyNode = new Node("Dummy"); // Only for storing rotation.  We don't actually rotate!  Todo - remove this once we've sorted out rotating bounding boxes

	public AbstractServerAvatar(IEntityController _module, ClientData _client, int _playerID, IInputDevice _input, int eid, IAvatarModel anim) {
		super(_module, _playerID, _input, eid, _client.side, anim);

		server = (AbstractGameServer)_module;
		client = _client;

		this.dummyNode.setLocalRotation(this.mainNode.getLocalRotation());
	}


	public void startAgain() {
		alive = true;
		this.moveSpeed = server.getAvatarMoveSpeed(this); // todo - send to client if it changes
		this.setJumpForce(server.getAvatarJumpForce(this)); // todo - send to client if it changes
		this.setHealth(server.getAvatarStartHealth(this));
		this.invulnerableTimeSecs = 5;
		server.moveAvatarToStartPosition(this); // this also sends the update message to tell the client about the new move speed values etc...

	}


	@Override
	public void processByServer(AbstractEntityServer server, float tpf) {
		if (!this.alive) {
			restartTimeSecs -= tpf;
			this.currentAnimCode = ANIM_DIED;
			if (this.restartTimeSecs <= 0) {
				Globals.p("Resurrecting avatar");
				this.startAgain();				
				server.gameNetworkServer.sendMessageToAll(new AvatarStartedMessage(this));
				if (Globals.DEBUG_PLAYER_RESTART) {
					Globals.p("Sent AvatarStartedMessage");
				}

				// Send position update
				EntityUpdateMessage eum = new EntityUpdateMessage();
				eum.addEntityData(this, true);
				server.gameNetworkServer.sendMessageToAll(eum);
			}
		} else {

			if (invulnerableTimeSecs >= 0) {
				invulnerableTimeSecs -= tpf;
			}

			super.serverAndClientProcess(server, null, tpf, System.currentTimeMillis());

			// Point us in the right direction
			Vector3f lookAtPoint = this.getMainNode().getWorldTranslation().add(input.getDirection());// camLeft.add(camDir.mult(10));
			lookAtPoint.y = this.getMainNode().getWorldTranslation().y; // Look horizontal!
			//this.getMainNode().lookAt(lookAtPoint, Vector3f.UNIT_Y); // need this in order to send the avatar's rotation to other players
			this.dummyNode.setLocalTranslation(this.getMainNode().getWorldTranslation());
			this.dummyNode.lookAt(lookAtPoint, Vector3f.UNIT_Y); // need this in order to send the avatar's rotation to other players

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
	public Quaternion getWorldRotation() {
		//return this.getMainNode().getLocalRotation();
		return this.dummyNode.getLocalRotation();//.rotation;
	}


	@Override
	public void setWorldRotation(final Quaternion newRot2) {
		//getMainNode().setLocalRotation(newRot2);
		this.dummyNode.setLocalRotation(newRot2.clone()); // Don't rotate the model!  This causes the boundingbox to expand.
	}


	@Override
	public void damaged(float amt, ICausesHarmOnContact collider, String reason) {
		if (server.gameData.getGameStatus() == SimpleGameData.ST_STARTED) {
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
		alive = true;
		this.setHealth(server.getAvatarStartHealth(this));
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
		//this.server.gameNetworkServer.sendMessageToClient(client, new AvatarStatusMessage(this, client));
		this.sendStatusUpdateMessage(false);
	}


	@Override
	public void decHealth(float h) {
		super.decHealth(h);
		//this.server.gameNetworkServer.sendMessageToClient(client, new AvatarStatusMessage(this, client));
		this.sendStatusUpdateMessage(true);
	}


	@Override
	protected boolean acceptInput() {
		return true;
	}

}
