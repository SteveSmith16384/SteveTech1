package com.scs.stevetech1.entities;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.models.BeamLaserModel;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class BulletTrail extends PhysicalEntity implements IProcessByClient {

	private static final float DURATION = .5f;

	private float timeLeft = DURATION;

	public BulletTrail(IEntityController _game, int playerID,  Vector3f start, Vector3f end) {
		super(_game, _game.getNextEntityID(), Globals.BULLET_TRAIL, "BulletTrail", true, false, false);
/*
		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("playerID", playerID);
			creationData.put("start", start);
			creationData.put("end", end);
		}
*/
		BeamLaserModel laserNode = BeamLaserModel.Factory(game.getAssetManager(), start, end, ColorRGBA.White, !game.isServer(), "Textures/roblox.png", 0.004f, false);
		this.mainNode.attachChild(laserNode);

		this.getMainNode().setUserData(Globals.ENTITY, this);

		this.collideable = false;

	}

/*
	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		this.timeLeft -= tpf_secs;
		if (this.timeLeft <= 0) {
				//this.remove();
				game.markForRemoval(this.getID());
		}
	}
*/

	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		this.timeLeft -= tpf_secs;
		if (this.timeLeft <= 0) {
			//this.remove();
			game.markForRemoval(this.getID());
		}
	}


}
