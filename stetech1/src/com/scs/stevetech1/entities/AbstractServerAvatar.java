package com.scs.stevetech1.entities;

import com.jme3.math.Vector3f;
import com.scs.simplephysics.SimpleCharacterControl;
import com.scs.stevetech1.components.IAnimatedAvatarModel;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.netmessages.AvatarStartedMessage;
import com.scs.stevetech1.netmessages.AvatarStatusMessage;
import com.scs.stevetech1.netmessages.EntityKilledMessage;
import com.scs.stevetech1.netmessages.EntityUpdateMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public abstract class AbstractServerAvatar extends AbstractAvatar implements IDamagable, IRewindable {

	private AbstractGameServer server;

	public AbstractServerAvatar(IEntityController _module, int _playerID, IInputDevice _input, int eid, int side, IAnimatedAvatarModel anim) {
		super(_module, _playerID, _input, eid, side, anim);

		//client = _client;
		server = (AbstractGameServer)_module;

		SimpleCharacterControl<PhysicalEntity> simplePlayerControl = (SimpleCharacterControl<PhysicalEntity>)this.simpleRigidBody; 
		simplePlayerControl.setJumpForce(Globals.JUMP_FORCE); // Different to client side, since that doesn't have gravity!
	}

	
	public void startAgain() {
		alive = true;
		this.health = server.getAvatarStartHealth(this);
		this.invulnerableTimeSecs = 5;
		server.moveAvatarToStartPosition(this);

	}

	
	@Override
	public void processByServer(AbstractGameServer server, float tpf) {
		if (!this.alive) {
			restartTimeSecs -= tpf;
			if (this.restartTimeSecs <= 0) {
				Globals.p("Resurrecting avatar");
				this.startAgain();
				
				server.networkServer.sendMessageToAll(new AvatarStartedMessage(this));

				// Send position update
				EntityUpdateMessage eum = new EntityUpdateMessage();
				eum.addEntityData(this, true);
				server.networkServer.sendMessageToAll(eum);
			}
			return;
		}

		if (invulnerableTimeSecs >= 0) {
			invulnerableTimeSecs -= tpf;
		}

		super.serverAndClientProcess(server, null, tpf, System.currentTimeMillis());

		// Point us in the right direction
		Vector3f lookAtPoint = this.getMainNode().getWorldTranslation().add(input.getDirection());// camLeft.add(camDir.mult(10));
		lookAtPoint.y = this.getMainNode().getWorldTranslation().y; // Look horizontal!
		this.getMainNode().lookAt(lookAtPoint, Vector3f.UNIT_Y); // need this in order to send the avatar's rotation to other players

		if (getWorldTranslation().y < -1) {
			// Dropped off the edge?
			server.console.appendText(getName() + " has fallen off the edge");
			fallenOffEdge();
		}

		if (this instanceof IRewindable) {
			addPositionData();
		}
	}


	@Override
	public void damaged(float amt, IEntity killer, String reason) {
		if (this.alive && invulnerableTimeSecs < 0) {
			this.health -= amt;
			if (health <= 0) {
				setDied(killer, reason);
			} else {
				Globals.p("Player " + this.getID() + " wounded " + amt + ": " + reason);
				this.server.networkServer.sendMessageToAll(new AvatarStatusMessage(this));
			}
		}
	}


	private void setDied(IEntity killer, String reason) {
		Globals.p("Player " + this.getID() + " died: " + reason);
		this.alive = false;
		this.restartTimeSecs = server.gameOptions.restartTimeSecs;
		server.networkServer.sendMessageToAll(new EntityKilledMessage(this, killer));

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


}
