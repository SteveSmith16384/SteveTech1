package com.scs.stevetech1.server;

import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IDamagable;

public class ServerSideCollisionLogic {

	public ServerSideCollisionLogic() {
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
		/*
		if (a instanceof IRemoveOnContact) {
			IRemoveOnContact roc = (IRemoveOnContact)a;
			markForRemoval(roc);
		}
		if (b instanceof IRemoveOnContact) {
			IRemoveOnContact roc = (IRemoveOnContact)b;
			markForRemoval(roc);
		}
*/
	}
	
	
	private void checkForDamage(ICausesHarmOnContact choc, IDamagable id) {
		if (choc.getSide() != id.getSide()) {
			id.damaged(choc.getDamageCaused(), choc, "Hit by " + choc);
		}
		
	}

/*
	private void markForRemoval(IRemoveOnContact roc) {
		roc.removeOnContact();
	}
	*/
}
