package com.scs.stetech1.client.syncposition;

import com.jme3.math.Vector3f;
import com.scs.stetech1.components.IPhysicalEntity;
import com.scs.stetech1.server.Settings;

public class InstantPositionAdjustment implements ICorrectClientEntityPosition {

	public InstantPositionAdjustment() {

	}


	@Override
	public void adjustPosition(IPhysicalEntity pe, Vector3f offset) {
		if (offset.length() > Settings.SMALLEST_MOVE_DIST) {
			if (Settings.DEBUG_SYNC_POS) {
			Settings.p("Adjusting client avatar by " + offset);
			}
			pe.adjustWorldTranslation(offset);
		}
	}


}
