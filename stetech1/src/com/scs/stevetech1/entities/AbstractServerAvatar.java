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
	private ClientData client; // is this used?

	public AbstractServerAvatar(IEntityController _module, ClientData _client, int _playerID, IInputDevice _input, int eid, int side, IAnimatedAvatarModel anim) {
		super(_module, _playerID, _input, eid, side, anim);

		client = _client;
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
				//this.moveToStartPostion();
				alive = true;
				server.networkServer.sendMessageToAll(new AvatarStatusMessage(this));

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

		if (getWorldTranslation().y < -1) {
			// Dropped away?
			server.console.appendText(getName() + " has fallen off the edge");
			fallenOffEdge();
		}

		if (this instanceof IRewindable) {
			addPositionData();
		}
	}


	@Override
	public void damaged(float amt, String reason) {
		died(reason);
	}


	private void died(String reason) {
		Globals.p("Player died: " + reason);
		this.alive = false;
		try {
			this.restartTime = AbstractGameServer.properties.GetRestartTimeSecs();
		} catch (NullPointerException ex){
			ex.printStackTrace();
		}
		server.networkServer.sendMessageToAll(new AvatarStatusMessage(this));
		//invulnerableTime = RESTART_DUR*3;
	}


	@Override
	public void hasSuccessfullyHit(IEntity e) {
		//this.incScore(20, "shot " + e.toString());
		//numShotsHit++;
	}

/*
	public void moveToStartPostion() {
		Vector3f pos = server.getAvatarStartPosition(this);
		//Settings.p("Scheduling player to start position: " + pos);
		super.setWorldTranslation(pos);
		EntityUpdateMessage eum = new EntityUpdateMessage();
		eum.addEntityData(this, true);
		server.networkServer.sendMessageToAll(eum);
	}
*/

	@Override
	public Vector3f getShootDir() {
		return input.getDirection();
	}


	@Override
	public void fallenOffEdge() {
		if (this.alive) {
			Globals.p("playerID " + this.playerID + " has died due to falling off the edge (pos " + this.getWorldTranslation() + ")");
			died("Too low");
		}
	}


}
