package com.scs.stevetech1.components;

import com.scs.stevetech1.server.RayCollisionData;

/**
 * This is for abilities where the server should calculate hits in the past, e.g. a hitscan rifle.
 *
 */
public interface IHitscanWeapon {

	float getRange();
	
	void setTarget(RayCollisionData pe);
	
}
