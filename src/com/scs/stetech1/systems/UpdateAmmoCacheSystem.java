package com.scs.stetech1.systems;

import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.components.IRequiresAmmoCache;
import com.scs.stetech1.netmessages.RequestNewBulletMessage;

public class UpdateAmmoCacheSystem extends AbstractSystem {

	public UpdateAmmoCacheSystem() {
	}

	
	@Override
	public void process(IEntity entity, float tpf_secs) {
		if (entity instanceof IRequiresAmmoCache) {
			IRequiresAmmoCache irac = (IRequiresAmmoCache)entity;
			RequestNewBulletMessage rnbm = new RequestNewBulletMessage();
			// todo - send
		}
		
	}

}
