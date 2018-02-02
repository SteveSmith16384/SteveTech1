package com.scs.stevetech1.entities;

import com.jme3.math.Vector3f;
import com.scs.simplephysics.SimpleCharacterControl;
import com.scs.stevetech1.components.IAnimatedAvatarModel;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.netmessages.AvatarStatusMessage;
import com.scs.stevetech1.netmessages.EntityUpdateMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
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


	@Override
	public void processByServer(AbstractGameServer server, float tpf) {
		if (!this.alive) {
			restartTime -= tpf;
			if (this.restartTime <= 0) {
				server.moveAvatarToStartPosition(this);

				alive = true;
				server.networkServer.sendMessageToAll(new AvatarStatusMessage(this));

				// Send position udpate
				EntityUpdateMessage eum = new EntityUpdateMessage();
				eum.addEntityData(this, true);
				server.networkServer.sendMessageToAll(eum);
			}
			return;
		}

		if (invulnerableTime >= 0) {
			invulnerableTime -= tpf;
		}
		
		super.serverAndClientProcess(server, null, tpf, System.currentTimeMillis());

		// Point us in the right direction
		//if (this.game.isServer()) {
			Vector3f lookAtPoint = this.getMainNode().getWorldTranslation().add(input.getDirection());// camLeft.add(camDir.mult(10));
			lookAtPoint.y = this.getMainNode().getWorldTranslation().y; // Look horizontal!
			this.getMainNode().lookAt(lookAtPoint, Vector3f.UNIT_Y); // need this in order to send the avatar's rotation to other players
		//}

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
	public void damaged(float amt, String reason) {
		setDied(reason);
	}


	private void setDied(String reason) {
		Globals.p("Player died: " + reason);
		this.alive = false;
		this.restartTime = server.gameOptions.restartTimeSecs;//.getRestartTimeSecs(); // AbstractGameServer.properties.GetRestartTimeSecs();
		server.networkServer.sendMessageToAll(new AvatarStatusMessage(this));
		
		avatarModel.setAnimationForCode(ANIM_DIED); // Send death as an anim, so it gets scheduled and is not shown straight away
		//invulnerableTime = RESTART_DUR*3;
	}


	@Override
	public Vector3f getShootDir() {
		return input.getDirection();
	}


	@Override
	public void fallenOffEdge() {
		if (this.alive) {
			Globals.p("playerID " + this.playerID + " has died due to falling off the edge (pos " + this.getWorldTranslation() + ")");
			setDied("fallen Off Edge");
		}
	}


}
