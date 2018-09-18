package com.scs.undercoveragent.systems.client;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.undercoveragent.entities.FallingSnowflake;

import ssmith.lang.NumberFunctions;
import ssmith.util.RealtimeInterval;

public class FallingSnowflakeSystem {

	private AbstractGameClient client;
	private RealtimeInterval snowflakeInt = new RealtimeInterval(50);

	public FallingSnowflakeSystem(AbstractGameClient _client) {
		super();

		client = _client;
	}


	public void process(float tpf_secs) {
		if (client.currentAvatar != null) {
			if (snowflakeInt.hitInterval()) {
				Vector3f pos = client.currentAvatar.getWorldTranslation().clone();
				pos.x += NumberFunctions.rndFloat(-5, 5);
				pos.y = 10;
				pos.z += NumberFunctions.rndFloat(-5, 5);
				FallingSnowflake snowflake = new FallingSnowflake(client, client.getNextEntityID(), pos);
				client.addEntity(snowflake);
			}
		}
	}

}
