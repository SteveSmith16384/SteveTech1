package com.scs.stevetech1.client.syncposition;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.components.IPhysicalEntity;
import com.scs.stevetech1.server.Globals;

public class InstantPositionAdjustment implements ICorrectClientEntityPosition {

	public InstantPositionAdjustment() {

	}


	@Override
	public void adjustPosition(IPhysicalEntity pe, Vector3f offset) {
		if (offset.length() > Globals.SMALLEST_MOVE_DIST) {
			if (Globals.DEBUG_ADJ_AVATAR_POS) {
				Globals.p("Adjusting client avatar by " + offset);
			}
			pe.adjustWorldTranslation(offset);
		}
	}


}
