package com.scs.stetech1.entities;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stetech1.client.AbstractGameClient;
import com.scs.stetech1.components.IAffectedByPhysics;
import com.scs.stetech1.components.ICollideable;
import com.scs.stetech1.components.IProcessByClient;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityTypes;
import com.scs.stetech1.shared.IEntityController;

public abstract class AbstractEnemyAvatar extends PhysicalEntity implements IAffectedByPhysics, ICollideable, IProcessByClient {// Need ICollideable so lasers don't bounce off it

	public AbstractEnemyAvatar(IEntityController game, int pid, int eid, float x, float y, float z) {
		super(game, eid, EntityTypes.AVATAR, "EnemyAvatar");

		/*if (game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("id", eid);
			creationData.put("playerID", eid);
		}*/

		Spatial geometry = getPlayersModel(game, pid);

		this.mainNode.attachChild(geometry);
		//float rads = (float)Math.toRadians(rotDegrees);
		//main_node.rotate(0, rads, 0);

		game.getRootNode().attachChild(this.mainNode);

		geometry.setUserData(Settings.ENTITY, this);
		mainNode.setUserData(Settings.ENTITY, this);

		game.addEntity(this);

		this.setWorldTranslation(new Vector3f(x, y, z));

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), this);

	}


	protected abstract Spatial getPlayersModel(IEntityController game, int pid);


	@Override
	public void process(AbstractGameServer sevrer, float tpf) {
		//Settings.p("Pos: " + this.getLocation());
	}

/*
	@Override
	public boolean collidedWith(ICollideable other) {
		// Do nothing
		return false;
	}

*/
	@Override
	public boolean hasMoved() {
		return true; // Always calc for avatars
	}


	@Override
	public void process(AbstractGameClient client, float tpf_secs) {
		// Do nothing
	}


}
