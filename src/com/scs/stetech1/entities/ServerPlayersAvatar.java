package com.scs.stetech1.entities;

import com.jme3.math.Vector3f;
import com.scs.stetech1.components.ICausesHarmOnContact;
import com.scs.stetech1.components.IDamagable;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.components.IRewindable;
import com.scs.stetech1.input.IInputDevice;
import com.scs.stetech1.netmessages.EntityUpdateMessage;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.IEntityController;

public abstract class ServerPlayersAvatar extends AbstractAvatar implements IDamagable, IRewindable {

	private AbstractGameServer server;

	public ServerPlayersAvatar(IEntityController _module, int _playerID, IInputDevice _input, int eid, int side) {
		super(_module, _playerID, _input, eid, side);

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

		super.serverAndClientProcess(server, null, tpf);
	}


	@Override
	public void damaged(float amt, String reason) {
		died(reason);
	}

/*
	public void hitByBullet_(ICausesHarmOnContact bullet) {
		if (invulnerableTime <= 0) {
			float dam = bullet.getDamageCaused();
			if (dam > 0) {
				Settings.p("Player hit by bullet");
				//module.doExplosion(this.main_node.getWorldTranslation(), this);
				//module.audioExplode.play();
				//this.health -= dam;
				//this.hud.setHealth(this.health);
				//this.hud.showDamageBox();

				died("hit by " + bullet.toString());
			}
		} else {
			Settings.p("Player hit but is currently invulnerable");
		}
	}
*/

	private void died(String reason) {
		Settings.p("Player died: " + reason);
		this.restarting = true;
		try {
			this.restartTime = AbstractGameServer.properties.GetRestartTimeSecs();
		} catch (NullPointerException ex){
			ex.printStackTrace();
		}
		//invulnerableTime = RESTART_DUR*3;

		// Move us below the map
		Vector3f pos = this.getMainNode().getWorldTranslation().clone();//.floor_phy.getPhysicsLocation().clone();
		pos.y = -10;//-SimpleCity.FLOOR_THICKNESS * 2;
		super.setWorldTranslation(pos);
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
			Settings.p("playerID " + this.playerID + " has died due to falling off the edge (pos " + this.getWorldTranslation() + ")");
			died("Too low");
		}
	}

	
}
