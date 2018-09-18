package com.scs.stevetech1.server;

import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.entities.PhysicalEntity;

public final class ServerSideCollisionLogic {

	private AbstractGameServer server;

	public ServerSideCollisionLogic(AbstractGameServer _server) {
		server = _server;
	}


	public void collision(PhysicalEntity a, PhysicalEntity b) {
		/*if (a.isMarkedForRemoval() || b.isMarkedForRemoval()) {
			return;
		}*/

		if (a instanceof INotifiedOfCollision) {
			INotifiedOfCollision ic = (INotifiedOfCollision)a;
			ic.notifiedOfCollision(b);
		}
		if (b instanceof INotifiedOfCollision) {
			INotifiedOfCollision ic = (INotifiedOfCollision)b;
			ic.notifiedOfCollision(a);
		}

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
				if (id.canBeDamaged()) {
					float damage = Math.max(0,  choc.getDamageCaused());
					id.damaged(damage, (IEntity)choc, "Hit by " + choc);
				}
			}
		}
	}

}
