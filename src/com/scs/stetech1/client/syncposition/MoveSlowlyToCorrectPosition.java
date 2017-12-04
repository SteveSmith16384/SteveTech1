package com.scs.stetech1.client.syncposition;

import com.jme3.math.Vector3f;
import com.scs.stetech1.components.IPhysicalEntity;
import com.scs.stetech1.server.Settings;

public class MoveSlowlyToCorrectPosition implements ICorrectClientEntityPosition {

	private static final float MAX_DIST = 0.1f;

	public MoveSlowlyToCorrectPosition() {
	}


	@Override
	public void adjustPosition(IPhysicalEntity pe, Vector3f offset) {
		float diff = offset.length();
		if (diff > Settings.SMALLEST_MOVE_DIST) { // Avoid lots of small movements
			if (diff > MAX_DIST) {
				offset.normalizeLocal().multLocal(MAX_DIST); // Move a smaller amount
			}
			Settings.p("Adjusting client avatar by " + offset);
			pe.adjustWorldTranslation(offset);
		}
	}


}
