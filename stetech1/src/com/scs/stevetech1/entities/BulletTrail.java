package com.scs.stevetech1.entities;

import java.util.HashMap;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.models.BeamLaserModel;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class BulletTrail extends PhysicalEntity implements IProcessByClient {

	private static final float DURATION = 3;

	private float timeLeft = DURATION;

	public BulletTrail(IEntityController _game, int id, int type, ICanShoot shooter, PhysicalEntity target, Vector3f end) {//, ColorRGBA col, String tex) {
		super(_game, id, type, "BulletTrail", true);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("shooterID", shooter.getID());
			creationData.put("targetID", target.getID());
			creationData.put("end", end);
			//creationData.put("col", col);
			//creationData.put("tex", tex);
		}

		Vector3f start = shooter.getBulletStartPos();
		if (target != null) { // If there's a target, override end
			end = target.getWorldTranslation(); // todo - shoot at centre!
		}

		BeamLaserModel laserNode = BeamLaserModel.Factory(game.getAssetManager(), start, end, ColorRGBA.Red, !game.isServer(), "Textures/greensun.png");
		this.mainNode.attachChild(laserNode);

		this.getMainNode().setUserData(Globals.ENTITY, this);

		this.collideable = false;

	}


	@Override
	public void processByServer(AbstractEntityServer server, float tpf_secs) {
		this.timeLeft -= tpf_secs;
		if (this.timeLeft <= 0) {
			this.remove();
		}
	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		//if (this.getID() <= 0) { // Client-controlled
		this.timeLeft -= tpf_secs;
		if (this.timeLeft <= 0) {
			this.remove();
		}
	}


}
