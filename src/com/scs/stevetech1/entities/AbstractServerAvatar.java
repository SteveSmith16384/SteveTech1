package com.scs.stevetech1.entities;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.animation.IGetAvatarAnimationString;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.netmessages.EntityUpdateMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public abstract class AbstractServerAvatar extends AbstractAvatar implements IDamagable, IRewindable {

	private AbstractGameServer server;

	public AbstractServerAvatar(IEntityController _module, int _playerID, IInputDevice _input, int eid, int side, IGetAvatarAnimationString animCodes) {
		super(_module, _playerID, _input, eid, side, animCodes);

		server = (AbstractGameServer)_module;
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf) {
		if (this.restarting) {
			restartTime -= tpf;
			if (this.restartTime <= 0) {
				this.moveToStartPostion(true);
				restarting = false;
				return;
			}
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
		this.restarting = true;
		try {
			this.restartTime = AbstractGameServer.properties.GetRestartTimeSecs();
		} catch (NullPointerException ex){
			ex.printStackTrace();
		}
		//invulnerableTime = RESTART_DUR*3;
	}


	@Override
	public void hasSuccessfullyHit(IEntity e) {
		//this.incScore(20, "shot " + e.toString());
		//new AbstractHUDImage(game, module, this.hud, "Textures/text/hit.png", this.hud.hud_width, this.hud.hud_height, 2);
		//this.hud.showCollectBox();
		//numShotsHit++;
	}


	public void moveToStartPostion(boolean invuln) {
		Vector3f pos = server.getAvatarStartPosition(this);
		//Settings.p("Scheduling player to start position: " + pos);
		super.setWorldTranslation(pos);
		if (invuln) {
			// invulnerableTime = Sorcerers.properties.GetInvulnerableTimeSecs();
		}
		EntityUpdateMessage eum = new EntityUpdateMessage();
		eum.addEntityData(this, true);
		server.networkServer.sendMessageToAll(eum);
	}


	@Override
	public Vector3f getShootDir() {
		return input.getDirection();
	}


	@Override
	public void fallenOffEdge() {
		if (!this.restarting) {
			Globals.p("playerID " + this.playerID + " has died due to falling off the edge (pos " + this.getWorldTranslation() + ")");
			died("Too low");
		}
	}

	
}
