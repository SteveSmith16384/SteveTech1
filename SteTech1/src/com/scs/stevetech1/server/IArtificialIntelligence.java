package com.scs.stevetech1.server;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.ITargetable;
import com.scs.stevetech1.entities.PhysicalEntity;

public interface IArtificialIntelligence {

	void process(AbstractGameServer server, float tpf_secs);
	
	Vector3f getDirection(); // Current dir to point entity the right way
	
	ITargetable getCurrentTarget();
	
	int getAnimCode();
	
	void collided(PhysicalEntity pe);
	
	void wounded(ICausesHarmOnContact collider);
}
