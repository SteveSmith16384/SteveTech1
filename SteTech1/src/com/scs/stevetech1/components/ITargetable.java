package com.scs.stevetech1.components;

import com.scs.stevetech1.entities.PhysicalEntity;

public interface ITargetable {

	boolean isValidTargetForSide(int shootersSide);
	
	boolean isAlive();
	
	//Vector3f getWorldTranslation();
	//PhysicalEntity getPhysicalEntity();
	
}
