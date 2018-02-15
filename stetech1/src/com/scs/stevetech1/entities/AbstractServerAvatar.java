package com.scs.stevetech1.entities;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.scs.simplephysics.SimpleCharacterControl;
import com.scs.stevetech1.components.IAvatarModel;
import com.scs.stevetech1.components.ICanScorePoints;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IGetReadyForGame;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.netmessages.AvatarStartedMessage;
import com.scs.stevetech1.netmessages.AvatarStatusMessage;
import com.scs.stevetech1.netmessages.EntityKilledMessage;
import com.scs.stevetech1.netmessages.EntityUpdateMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public abstract class AbstractServerAvatar extends AbstractAvatar implements IDamagable, IRewindable, IGetReadyForGame, ICanScorePoints {

	private AbstractGameServer server;
	//private Quaternion rotation; // Store it so we don't rotate the spatial, but can send rotation to other players
	private Spatial dummyNode = new Node("Dummy"); // Only for storing rotation

	public AbstractServerAvatar(IEntityController _module, int _playerID, IInputDevice _input, int eid, int side, IAvatarModel anim) {
		super(_module, _playerID, _input, eid, side, anim);

		server = (AbstractGameServer)_module;

		SimpleCharacterControl<PhysicalEntity> simplePlayerControl = (SimpleCharacterControl<PhysicalEntity>)this.simpleRigidBody; 
		simplePlayerControl.setJumpForce(Globals.JUMP_FORCE); // Different to client side, since that doesn't have gravity!
		
		this.dummyNode.setLocalRotation(this.mainNode.getLocalRotation());
	}


	public void startAgain() {
		alive = true;
		this.setHealth(server.getAvatarStartHealth(this));
		this.invulnerableTimeSecs = 5;
		server.moveAvatarToStartPosition(this);

	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf) {
		if (this.statsChanged) {
			this.server.gameNetworkServer.sendMessageToAll(new AvatarStatusMessage(this));
			this.statsChanged = false;
		}


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
				server.console.appendText(getName() + " has fallen off the edge");
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


	private void setDied(IEntity killer, String reason) {
		Globals.p("Player " + this.getID() + " died: " + reason);
		this.alive = false;
		this.restartTimeSecs = server.gameOptions.avatarRestartTimeSecs;
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
		this.setScore(0);
		this.invulnerableTimeSecs = 5;

	}

}
