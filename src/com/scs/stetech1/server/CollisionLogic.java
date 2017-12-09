package com.scs.stetech1.server;

import com.scs.stetech1.components.ICausesHarmOnContact;
import com.scs.stetech1.components.IDamagable;
import com.scs.stetech1.components.IRemoveOnContact;
import com.scs.stetech1.entities.PhysicalEntity;

public class CollisionLogic {

	public CollisionLogic() {
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
		
		if (a instanceof IRemoveOnContact) {
			IRemoveOnContact roc = (IRemoveOnContact)a;
			checkForRemoval(roc);
		}
		if (b instanceof IRemoveOnContact) {
			IRemoveOnContact roc = (IRemoveOnContact)b;
			checkForRemoval(roc);
		}
	}
	
	
	private void checkForDamage(ICausesHarmOnContact choc, IDamagable id) {
		if (choc.getSide() != id.getSide()) {
			id.damaged(choc.getDamageCaused(), "Hit by " + choc);
		}
		
	}


	private void checkForRemoval(IRemoveOnContact roc) {
		roc.remove();
	}
	
}
