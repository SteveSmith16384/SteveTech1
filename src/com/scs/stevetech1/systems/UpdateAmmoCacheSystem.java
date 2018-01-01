package com.scs.stevetech1.systems;

import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IRequiresAmmoCache;
import com.scs.stevetech1.netmessages.RequestNewBulletMessage;

public class UpdateAmmoCacheSystem extends AbstractSystem {

	private AbstractGameClient server;

	public UpdateAmmoCacheSystem(AbstractGameClient _server) {
		super();

		server = _server;
	}


	@Override
	public void process(IEntity entity, float tpf_secs) {
		if (entity instanceof IRequiresAmmoCache) {
			IRequiresAmmoCache irac = (IRequiresAmmoCache)entity;
			if (irac.requiresAmmo()) {
				RequestNewBulletMessage rnbm = new RequestNewBulletMessage(irac.getAmmoType(), entity.getID());
				server.networkClient.sendMessageToServer(rnbm);
			}
		}

	}

}