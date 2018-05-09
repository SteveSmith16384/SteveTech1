package com.scs.stevetech1.server;

import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.data.SimpleGameData;

public class ServerSideCollisionLogic {

	private AbstractGameServer server;

	public ServerSideCollisionLogic(AbstractGameServer _server) {
		server = _server;
	}


	public void collision(Object a, Object b) {
		if (a instanceof ICausesHarmOnContact && b instanceof IDamagable) {
			ICausesHarmOnContact choc = (ICausesHarmOnContact)a;
			IDamagable id = (IDamagable)b;
			checkForDamage(choc, id);
		}
		if (b instanceof ICausesHarmOnContact && a instanceof IDamagable) {
			ICausesHarmOnContact choc = (ICausesHarmOnContact)b;
			IDamagable id = (IDamagable)a;
			checkForDamage(choc, id);
		}
	}


	private void checkForDamage(ICausesHarmOnContact choc, IDamagable id) {
		if (server.getGameData().getGameStatus() == SimpleGameData.ST_STARTED) {
			if (choc.getSide() != id.getSide()) {
				id.damaged(choc.getDamageCaused(), choc, "Hit by " + choc);
			}
		}
	}

}
