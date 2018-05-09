package com.scs.stevetech1.entities;

import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.ILaunchable;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.server.RayCollisionData;
import com.scs.stevetech1.shared.IEntityController;

public abstract class AbstractAIBullet extends PhysicalEntity implements ICausesHarmOnContact, ILaunchable, INotifiedOfCollision {

	public IEntity shooter; // So we know who not to collide with
	private int side;
	protected boolean useRay;
	private Vector3f dir;
	protected float speed;
	protected Vector3f origin;
	protected float range;

	//private float distLeft = 30f;

	public AbstractAIBullet(IEntityController _game, int id, int type, float x, float y, float z, String name, int _side, IEntity _shooter, Vector3f _dir, boolean _useRay, float _speed, float _range) {
		super(_game, id, type, name, true, false);

		side = _side;
		shooter = _shooter;
		dir = _dir;
		useRay = _useRay;
		speed = _speed;
		origin = new Vector3f(x, y, z);
		range = _range;
		
		if (Globals.STRICT) {
			if (side <= 0) {
				throw new RuntimeException("Invalid side: " + side);
			}
		}

		this.createBulletModel(dir);
		this.setWorldTranslation(new Vector3f(x, y, z));

		if (Globals.DEBUG_AI_BULLET_POS) {
			Globals.p("AI bullet " + this.getID() + " starting at " + this.getWorldTranslation());
		}
	}


	public float getDistanceTravelled() {
		 return this.origin.distance(this.getWorldTranslation());
	}


	protected abstract void createBulletModel(Vector3f dir);


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		if (!useRay) {
			super.processByServer(server, tpf_secs);
		} else {
			Ray ray = new Ray(this.getWorldTranslation(), dir);
			ray.setLimit(speed * tpf_secs);
			RayCollisionData rcd = this.checkForRayCollisions(ray);
			if (rcd != null) {
				this.remove();
				server.collisionOccurred(this, rcd.entityHit);
			} else {
				// Move spatial
				Vector3f offset = this.dir.mult(speed * tpf_secs);
				this.adjustWorldTranslation(offset);
				if (Globals.DEBUG_AI_BULLET_POS) {
					Globals.p("AI Bullet " + this.getID() + " moved by " + offset.length());
				}
			}
		}

		if (!this.removed) {
			if (Globals.DEBUG_AI_BULLET_POS) {
				Globals.p("AI Bullet " + this.getID() + " is at " + this.getWorldTranslation());
			}

			if (range > 0) {
				float dist = this.origin.distance(this.getWorldTranslation());
				if (dist > range) {
					this.remove();
				}
			}
		}
	}


	@Override
	public int getSide() {
		return side;
	}


	@Override
	public IEntity getActualShooter() {
		return shooter;
	}


	@Override
	public void remove() {
		if (!removed) {
			if (Globals.DEBUG_AI_BULLET_POS) {
				Globals.p("Removing AI bullet " + this.getID());
			}

			super.remove();
		}
	}


	@Override
	public IEntity getLauncher() {
		return shooter;
	}


	@Override
	public void launch(IEntity _shooter, Vector3f startPos, Vector3f dir) {
		// Do nothing, already launched 

	}


	@Override
	public boolean hasBeenLaunched() {
		return true; // Always launched immed
	}


}
