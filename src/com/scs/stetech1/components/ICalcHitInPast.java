package com.scs.stetech1.components;

import com.scs.stetech1.server.RayCollisionData;

/**
 * This is for abilities where the server should calculate hits in the past, e.g. a hitscan rifle.
 *
 */
public interface ICalcHitInPast {

	float getRange();
	
	void setTarget(RayCollisionData pe);
	
}
