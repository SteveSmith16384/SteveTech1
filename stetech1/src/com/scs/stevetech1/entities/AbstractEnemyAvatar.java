package com.scs.stevetech1.entities;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IAffectedByPhysics;
import com.scs.stevetech1.components.IAvatarModel;
import com.scs.stevetech1.components.IClientSideAnimated;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

/*
 * This is only used client-side
 */
public abstract class AbstractEnemyAvatar extends PhysicalEntity implements IAffectedByPhysics, IClientSideAnimated {
	
	protected IAvatarModel anim;
	//public int side;

	public AbstractEnemyAvatar(IEntityController game, int type, int pid, int eid, float x, float y, float z, IAvatarModel _anim, int _side) {
		super(game, eid, type, "EnemyAvatar", true);

		anim = _anim;
		//side = _side;
		
		Spatial geometry = anim.createAndGetModel(true, _side);// getPlayersModel(game, pid);

		this.mainNode.attachChild(geometry);

		geometry.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

		this.setWorldTranslation(new Vector3f(x, y, z));

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), true, this);
		simpleRigidBody.setGravity(0); // So they move exactly where we want, even when client jumps

	}


	@Override
	public boolean sendUpdates() {
		return true; // Always send for avatars
	}

}
