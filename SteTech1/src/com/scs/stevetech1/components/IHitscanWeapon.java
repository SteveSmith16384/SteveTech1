package com.scs.stevetech1.components;

import com.scs.stevetech1.server.RayCollisionData;

public interface IHitscanWeapon {

	float getRange();
	
	void setTarget(RayCollisionData pe);
	
}
