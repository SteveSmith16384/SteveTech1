package com.scs.stevetech1.client.syncposition;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.components.IPhysicalEntity;
import com.scs.stevetech1.server.Globals;

public class AdjustBasedOnDistance implements ICorrectClientEntityPosition {

	public AdjustBasedOnDistance() {
	}


	@Override
	public void adjustPosition(IPhysicalEntity pe, Vector3f offset) { // pe.getWorldTranslation();
		float diff = offset.length();
		if (Float.isNaN(diff)) {
			Globals.p("Emergency warp of player");
			pe.setWorldTranslation(new Vector3f(10, 10, 10)); // Emergency!
		} else if (diff > 4) {
			Globals.p("Far out! " + diff);
			// They're so far out, just move them
			pe.setWorldTranslation(pe.getWorldTranslation().add(offset)); 
		} else if (diff > Globals.SMALLEST_MOVE_DIST) { // Avoid lots of small movements
			if (Globals.DEBUG_SYNC_POS) {
				Globals.p("Adjusting client avatar by " + offset);
			}
			pe.adjustWorldTranslation(offset);
		}
	}


}
