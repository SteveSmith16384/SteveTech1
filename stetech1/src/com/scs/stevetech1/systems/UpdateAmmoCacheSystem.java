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


	//@Override
	public void process(IRequiresAmmoCache irac, float tpf_secs) {
		if (irac.requiresAmmo()) {
			RequestNewBulletMessage rnbm = new RequestNewBulletMessage(irac.getAmmoType(), irac.getID());
			server.networkClient.sendMessageToServer(rnbm);
		}

	}

}
