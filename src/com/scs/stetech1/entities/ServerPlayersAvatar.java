package com.scs.stetech1.entities;

import com.jme3.math.Vector3f;
import com.scs.stetech1.components.IBullet;
import com.scs.stetech1.components.ICollideable;
import com.scs.stetech1.components.IDamagable;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.input.IInputDevice;
import com.scs.stetech1.netmessages.EntityUpdateMessage;
import com.scs.stetech1.server.ServerMain;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityPositionData;
import com.scs.stetech1.shared.IEntityController;

public class ServerPlayersAvatar extends AbstractPlayersAvatar implements IDamagable, ICollideable {

	private ServerMain server;
	
	public ServerPlayersAvatar(IEntityController _module, int _playerID, IInputDevice _input, int eid) {
		super(_module, _playerID, _input, eid);
		
		server = (ServerMain)_module;
	}


	@Override
	public void process(ServerMain server, float tpf) {
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

		if (!this.restarting) {
			// Have we fallen off the edge
			if (this.getWorldTranslation().y < -5f) {
				//this.getMainNode().getWorldTranslation();
				Settings.p("playerID " + this.playerID + " has died due to falling off the edge (pos " + this.getWorldTranslation() + ")");
				died("Too low");
				return;
			}
		}

		super.serverAndClientProcess(server, null, tpf);
		
		EntityPositionData epd = new EntityPositionData();
		epd.serverTimestamp = System.currentTimeMillis();
		epd.rotation = this.getWorldRotation();
		epd.position = this.getWorldTranslation();
		addPositionData(epd); // Store the position for use when rewinding.

	}


	@Override
	public void damaged(float amt, String reason) {
		died(reason);
	}


	public void hitByBullet(IBullet bullet) {
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


	private void died(String reason) {
		Settings.p("Player died: " + reason);
		this.restarting = true;
		try {
			this.restartTime = ServerMain.properties.GetRestartTimeSecs();
		} catch (NullPointerException ex){
			ex.printStackTrace();
		}
		//invulnerableTime = RESTART_DUR*3;

		// Move us below the map
		Vector3f pos = this.getMainNode().getWorldTranslation().clone();//.floor_phy.getPhysicsLocation().clone();
		pos.y = -10;//-SimpleCity.FLOOR_THICKNESS * 2;
		playerControl.warp(pos);
	}


	@Override
	public void collidedWith(ICollideable other) {
		if (other instanceof IBullet) {
			IBullet bullet = (IBullet)other;
			if (bullet.getShooter() != null) {
				if (bullet.getShooter() != this) {
					if (!(bullet.getShooter() instanceof AbstractPlayersAvatar)) {
						this.hitByBullet(bullet);
						bullet.getShooter().hasSuccessfullyHit(this);
					}
				}
			}
		}
	}


	@Override
	public void hasSuccessfullyHit(IEntity e) {
		//this.incScore(20, "shot " + e.toString());
		//new AbstractHUDImage(game, module, this.hud, "Textures/text/hit.png", this.hud.hud_width, this.hud.hud_height, 2);
		//this.hud.showCollectBox();
		//numShotsHit++;
	}


	public void moveToStartPostion(boolean invuln) {
		//Point p = module.mapData.getPlayerStartPos(id);
		Vector3f warpPos = new Vector3f(3f, 15f, 3f + this.playerID);
		Settings.p("Scheduling player to start position: " + warpPos);
		this.playerControl.warp(warpPos);
		if (invuln) {
			// invulnerableTime = Sorcerers.properties.GetInvulnerableTimeSecs();
		}
		server.broadcast(new EntityUpdateMessage(this, true));
	}


	@Override
	public Vector3f getShootDir() {
		return input.getDirection();
	}


}
