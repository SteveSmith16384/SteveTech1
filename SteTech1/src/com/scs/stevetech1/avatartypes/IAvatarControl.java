package com.scs.stevetech1.avatartypes;

import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.entities.PhysicalEntity;

public interface IAvatarControl {

	void init(PhysicalEntity pe);
	
	SimpleRigidBody<PhysicalEntity> getSimpleRigidBody();
	
	void process();
	
	int getCurrentAnimCode();
	
	long getLastMoveTime();
	
	boolean jump();
}
