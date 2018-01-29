package com.scs.stevetech1.client.syncposition;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.components.IPhysicalEntity;
import com.scs.stevetech1.server.Globals;

public class AdjustByFractionOfDistance implements ICorrectClientEntityPosition {

	public AdjustByFractionOfDistance() {

	}


	@Override
	public boolean adjustPosition(IPhysicalEntity pe, Vector3f offset, float tpf_secs) {
		if (offset.length() > Globals.SMALLEST_MOVE_DIST) {
			if (Globals.DEBUG_ADJ_AVATAR_POS) {
				Globals.p("Adjusting client avatar by " + offset);
			}
			//offset.divideLocal(tpf_secs); // Need to divide it since we later multiply it
			//offset.divideLocal(2);
			pe.adjustWorldTranslation(offset);
			return true;
		}
		return false;
	}


}
