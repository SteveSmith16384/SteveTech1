package com.scs.stetech1.client.syncposition;

import com.jme3.math.Vector3f;
import com.scs.stetech1.components.IPhysicalEntity;

public class MoveSlowlyToCorrectPosition implements ICorrectClientEntityPosition {

	private float maxDist;
	
	public MoveSlowlyToCorrectPosition(float _maxDist) {
		maxDist = _maxDist;
	}

	
	@Override
	public void adjustPosition(IPhysicalEntity pe, Vector3f offset) {
		float diff = offset.length();
		if (diff > maxDist) {
			offset.normalizeLocal().multLocal(maxDist);
		}
		pe.adjustWorldTranslation(offset);
		//pe.setWorldTranslation(pe.getWorldTranslation().add(offset)); No!
	}


}
