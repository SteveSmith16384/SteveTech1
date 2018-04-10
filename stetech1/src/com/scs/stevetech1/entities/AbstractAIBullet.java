package com.scs.stevetech1.entities;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.shared.IEntityController;

public abstract class AbstractAIBullet extends PhysicalEntity implements ICausesHarmOnContact { // ILaunchable

	public IEntity shooter; // So we know who not to collide with
	private int side;

	public AbstractAIBullet(IEntityController _game, int id, int type, float x, float y, float z, String name, int _side, IEntity _shooter, Vector3f dir) {
		super(_game, id, type, name, true);

		side = _side;

		shooter = _shooter;

		this.createSimpleRigidBody(dir);

		game.getGameNode().attachChild(this.mainNode);
		this.setWorldTranslation(new Vector3f(x, y, z));
		this.mainNode.updateGeometricState();
	}

	
	protected abstract void createSimpleRigidBody(Vector3f dir); // todo - rename to createModel or something
	

	@Override
	public void processByServer(AbstractEntityServer server, float tpf_secs) {
		super.processByServer(server, tpf_secs);
	}

/*
	@Override
	public IEntity getLauncher() {
		return shooter;
	}
*/

	@Override
	public int getSide() {
		return side;
	}


	@Override
	public IEntity getActualShooter() {
		return shooter;
	}

}
