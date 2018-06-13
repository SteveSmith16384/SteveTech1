package com.scs.stevetech1.weapons;

import java.util.HashMap;

import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.scs.stevetech1.components.ICalcHitInPast;
import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.BulletTrail;
import com.scs.stevetech1.netmessages.AbilityUpdateMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.server.RayCollisionData;
import com.scs.stevetech1.shared.IEntityController;

public class HitscanRifle extends AbstractMagazineGun implements ICalcHitInPast, ICausesHarmOnContact {

	private static final int MAG_SIZE = 10;
	private static final float RANGE = 99f;

	private RayCollisionData hitThisMoment = null; // Only used server-side.  Null if nothing hit
	private int bulletsInMag = MAG_SIZE;

	public HitscanRifle(IEntityController game, int id, int type, int playerID, AbstractAvatar owner, int avatarID, int num, ClientData client) {
		super(game, id, type, playerID, owner, avatarID, num, "Hitscan Rifle", .2f, 1f, MAG_SIZE, client);
		
	}


	@Override
	public boolean launchBullet() {
		bulletsInMag--;
		if (game.isServer()) {
			// We have already calculated the hit as part of ICalcHitInPast
			AbstractGameServer server = (AbstractGameServer)game;
			if (hitThisMoment != null) {
				//Settings.p(hitThisMoment.entity + " has been shot!");

				server.collisionLogic.collision(hitThisMoment.entityHit, this);

				/*Vector3f pos = this.hitThisMoment.point;
				DebuggingSphere ds = new DebuggingSphere(game, game.getNextEntityID(), debugSphereType, pos.x, pos.y, pos.z, true, true); // Show where it hit
				game.addEntity(ds);*/

				//BulletTrail bt = new BulletTrail(game, game.getNextEntityID(), trailType, this.playerID, this.owner.getBulletStartPos(), hitThisMoment.point);
				//game.addEntity(bt);
				server.sendBulletTrail(this.playerID, this.owner.getBulletStartPos(), hitThisMoment.point);

				this.hitThisMoment = null; // Clear it ready for next loop
			} else {
				// Bullet trail into the sky
				Vector3f endPos = this.owner.getBulletStartPos().add(this.owner.getShootDir().mult(RANGE));
				/*BulletTrail bt = new BulletTrail(game, this.playerID, this.owner.getBulletStartPos(), endPos);
				game.addEntity(bt);*/
				server.sendBulletTrail(this.playerID, this.owner.getBulletStartPos(), endPos);

			}
		} else { // Client
			ICanShoot shooter = (ICanShoot)owner; 
			Vector3f from = shooter.getBulletStartPos();
			if (Globals.DEBUG_SHOOTING_POS) {
				Globals.p("Client shooting from " + from);
			}
			Ray ray = new Ray(from, shooter.getShootDir());
			ray.setLimit(RANGE);
			RayCollisionData rcd = shooter.checkForRayCollisions(ray);
			if (rcd != null) {
				Vector3f pos = rcd.point;
				Globals.p("Hit " + rcd.entityHit + " at " + pos);

				// Show where hit
				//DebuggingSphere ds = new DebuggingSphere(game, game.getNextEntityID(), debugSphereType, pos.x, pos.y, pos.z, false, true);
				//game.addClientOnlyEntity(ds);
				
				// Show bullet trails
				BulletTrail bt = new BulletTrail(game, this.playerID, this.owner.getBulletStartPos(), pos);
				game.addEntity(bt);
			} else {
				Globals.p("Not hit anything");
				// Bullet trail into the sky
				Vector3f endPos = this.owner.getBulletStartPos().add(this.owner.getShootDir().mult(RANGE));
				BulletTrail bt = new BulletTrail(game, this.playerID, this.owner.getBulletStartPos(), endPos);
				game.addEntity(bt);
			}
		}
		return true;
	}


	@Override
	public void setTarget(RayCollisionData hd) {
		this.hitThisMoment = hd;

	}


	@Override
	public float getRange() {
		return RANGE;
	}


	@Override
	public float getDamageCaused() {
		return 1;
	}


	@Override
	public int getSide() {
		return this.owner.getSide();
	}


	@Override
	public HashMap<String, Object> getCreationData() {
		return super.creationData;
	}


	@Override
	public void reload(AbstractGameServer server) {
		this.bulletsInMag = this.magazineSize;
	}
	
	
	@Override
	protected void createBullet(AbstractGameServer server, int entityid, int playerID, IEntityContainer irac, int side) {
		// No physical projectiles required!
		
	}


	@Override
	public int getBulletsInMag() {
		return bulletsInMag;
	}


	@Override
	public IEntity getActualShooter() {
		return owner;
	}


	@Override
	public void encode(AbilityUpdateMessage aum) {
		super.encode(aum);
		
		aum.bulletsLeftInMag = this.getBulletsInMag();

	}


	@Override
	public void decode(AbilityUpdateMessage aum) {
		super.decode(aum);
		
		this.bulletsInMag = aum.bulletsLeftInMag;
	}


	@Override
	protected void emptyMagazine() {
		this.bulletsInMag = 0;
		
	}


}