package com.scs.stevetech1.entities;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IAffectedByPhysics;
import com.scs.stevetech1.components.IAnimated;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

/*
 * This is only used client-side
 */
public abstract class AbstractEnemyAvatar extends PhysicalEntity implements IAffectedByPhysics, IAnimated {

	public AbstractEnemyAvatar(IEntityController game, int pid, int eid, float x, float y, float z) {
		super(game, eid, 1, "EnemyAvatar", true);

		Spatial geometry = getPlayersModel(game, pid);

		this.mainNode.attachChild(geometry);
		//float rads = (float)Math.toRadians(rotDegrees);
		//main_node.rotate(0, rads, 0);

		game.getRootNode().attachChild(this.mainNode);

		geometry.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

		game.addEntity(this);

		this.setWorldTranslation(new Vector3f(x, y, z));

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), true, this);

	}


	protected abstract Spatial getPlayersModel(IEntityController game, int pid);


	@Override
	public boolean hasMoved() {
		return true; // Always calc for avatars
	}


}
