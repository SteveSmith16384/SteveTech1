package com.scs.stevetech1.server;

import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.ITargetableByAI;
import com.scs.stevetech1.entities.PhysicalEntity;

public interface IArtificialIntelligence {

	void process(AbstractGameServer server, float tpfSecs);
	
	ITargetableByAI getCurrentTarget();
	
	int getAnimCode();
	
	void collided(PhysicalEntity pe);
	
	void wounded(IEntity collider);
}
