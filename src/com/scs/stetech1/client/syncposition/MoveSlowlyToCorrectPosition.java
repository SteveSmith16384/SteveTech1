package com.scs.stetech1.client.syncposition;

import com.jme3.math.Vector3f;

public class MoveSlowlyToCorrectPosition implements IPositionAdjuster {

	private float maxDist;
	
	public MoveSlowlyToCorrectPosition(float _maxDist) {
		maxDist = _maxDist;
	}

	@Override
	public void adjustAdjustment(Vector3f offset) {
		float diff = offset.length();
		if (diff > maxDist) {
			offset.normalizeLocal().multLocal(maxDist);
		}
	}


}
