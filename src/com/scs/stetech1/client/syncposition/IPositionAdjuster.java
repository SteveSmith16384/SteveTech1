package com.scs.stetech1.client.syncposition;

import com.jme3.math.Vector3f;

public interface IPositionAdjuster {

	Vector3f getNewAdjustment(Vector3f offset);
}
