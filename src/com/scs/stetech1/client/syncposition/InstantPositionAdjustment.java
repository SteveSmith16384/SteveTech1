package com.scs.stetech1.client.syncposition;

import com.jme3.math.Vector3f;
import com.scs.stetech1.components.IPhysicalEntity;

public class InstantPositionAdjustment implements ICorrectClientEntityPosition {

	public InstantPositionAdjustment() {

	}


	@Override
	public void adjustPosition(IPhysicalEntity pe, Vector3f offset) {
		//if (offset.length() > 0.01f) {
		//pe.setWorldTranslation(pe.getWorldTranslation().add(offset));  No!!
		pe.adjustWorldTranslation(offset);
		//}
	}


}
