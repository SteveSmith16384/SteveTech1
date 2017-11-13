package com.scs.stetech1.client.syncposition;

import com.jme3.math.Vector3f;
import com.scs.stetech1.components.IPhysicalEntity;

public class AdjustBasedOnDistance implements ICorrectClientEntityPosition {

	public AdjustBasedOnDistance() {
	}

	
	@Override
	public void adjustPosition(IPhysicalEntity pe, Vector3f offset) {
		//float diff = offset.length();
		pe.adjustWorldTranslation(offset.mult(.1f));
	}


}
