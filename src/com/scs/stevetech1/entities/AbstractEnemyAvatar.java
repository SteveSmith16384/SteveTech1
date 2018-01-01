package com.scs.stevetech1.entities;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IAffectedByPhysics;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.testgame.TestGameEntityCreator;

public abstract class AbstractEnemyAvatar extends PhysicalEntity implements IAffectedByPhysics, IProcessByClient {

	public AbstractEnemyAvatar(IEntityController game, int pid, int eid, float x, float y, float z) {
		super(game, eid, 1, "EnemyAvatar");

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

		geometry.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

		game.addEntity(this);

		this.setWorldTranslation(new Vector3f(x, y, z));

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), this);

	}


	protected abstract Spatial getPlayersModel(IEntityController game, int pid);


	@Override
	public void processByServer(AbstractGameServer sevrer, float tpf) {
		//Settings.p("Pos: " + this.getLocation());
	}


	@Override
	public boolean hasMoved() {
		return true; // Always calc for avatars
	}


	@Override
	public void processByClient(AbstractGameClient client, float tpf_secs) {
		// Animate?
	}


}