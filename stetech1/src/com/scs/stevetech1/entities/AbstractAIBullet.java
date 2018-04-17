package com.scs.stevetech1.entities;

import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.ILaunchable;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.server.RayCollisionData;
import com.scs.stevetech1.shared.IEntityController;

public abstract class AbstractAIBullet extends PhysicalEntity implements ICausesHarmOnContact, ILaunchable {

	public IEntity shooter; // So we know who not to collide with
	private int side;
	protected boolean useRay;
	private Vector3f dir;
	protected float speed;
	
	private float distLeft = 30f;

	public AbstractAIBullet(IEntityController _game, int id, int type, float x, float y, float z, String name, int _side, IEntity _shooter, Vector3f _dir, boolean _useRay, float _speed) {
		super(_game, id, type, name, true);

		side = _side;
		shooter = _shooter;
		dir = _dir;
		useRay = _useRay;
		speed = _speed;

		this.createSimpleRigidBody(dir);
		this.setWorldTranslation(new Vector3f(x, y, z));
	}


	protected abstract void createSimpleRigidBody(Vector3f dir); // todo - rename to createModel or something


	@Override
	public void processByServer(AbstractEntityServer server, float tpf_secs) {
		if (!useRay) {
			super.processByServer(server, tpf_secs);
		} else {
			Ray ray = new Ray(this.getWorldTranslation(), dir);
			ray.setLimit(speed * tpf_secs);
			RayCollisionData rcd = this.checkForCollisions(ray);
			if (rcd != null) {
				Globals.p("Bullet hit " + rcd.entity);
				DebuggingSphere ds = new DebuggingSphere(server, server.getNextEntityID(), 16, rcd.point.x, rcd.point.y, rcd.point.z, true, false);
				server.addEntity(ds);
				this.remove();
				//server.co
				// todo
			} else {
				// Move spatial
				Vector3f offset = this.dir.mult(speed * tpf_secs);
				this.adjustWorldTranslation(offset);
			}
		}

		this.distLeft -= (speed * tpf_secs);
		if (this.distLeft < 0) {
			this.remove();
		}

		if (Globals.DEBUG_AI_SHOOTING) {
			Globals.p("AI Bullet at " + this.getWorldTranslation());
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
		if (Globals.DEBUG_AI_SHOOTING) {
			Globals.p("Removing bullet");
		}

		super.remove();
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
