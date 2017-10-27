package com.scs.stetech1.server.entities;

import com.jme3.math.Vector3f;
import com.scs.stetech1.components.IBullet;
import com.scs.stetech1.components.ICollideable;
import com.scs.stetech1.components.IDamagable;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.input.IInputDevice;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.AbstractPlayersAvatar;
import com.scs.stetech1.shared.IEntityController;

public class ServerPlayersAvatar extends AbstractPlayersAvatar implements IDamagable, ICollideable {

	protected boolean restarting = false;
	protected float restartTime, invulnerableTime;
	private int numShots = 0;
	private int numShotsHit = 0;


	public ServerPlayersAvatar(IEntityController _module, int _playerID, IInputDevice _input) {
		super(_module, _playerID, _input);
	}


	@Override
	public void process(float tpf) {
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
			if (this.playerControl.getPhysicsRigidBody().getPhysicsLocation().y < -5f) {
				died("Too low");
				return;
			}
		}
		
		super.process(tpf);
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
		this.restartTime = 3; // todo - why is this null? ServerMain.properties.GetRestartTimeSecs();
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
		this.incScore(20, "shot " + e.toString());
		//new AbstractHUDImage(game, module, this.hud, "Textures/text/hit.png", this.hud.hud_width, this.hud.hud_height, 2);
		//this.hud.showCollectBox();
		numShotsHit++;
		calcAccuracy();
	}


	private void calcAccuracy() {
		int a = (int)((this.numShotsHit * 100f) / this.numShots);
		//hud.setAccuracy(a);
	}


	public void incScore(float amt, String reason) {
		Settings.p("Inc score: +" + amt + ", " + reason);
		this.score += amt;
		//this.hud.setScore(this.score);

	}


	public void moveToStartPostion(boolean invuln) {
		//Point p = module.mapData.getPlayerStartPos(id);
		Vector3f warpPos = new Vector3f(3f, 5f, 3f + this.playerID);
		Settings.p("Scheduling player to start position: " + warpPos);
		this.playerControl.warp(warpPos);
		if (invuln) {
			// invulnerableTime = Sorcerers.properties.GetInvulnerableTimeSecs();
		}
	}



	@Override
	public Vector3f getShootDir() {
		return input.getDirection();
	}


}
