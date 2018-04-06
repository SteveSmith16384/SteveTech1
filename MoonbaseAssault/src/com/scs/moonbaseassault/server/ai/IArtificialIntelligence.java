package com.scs.moonbaseassault.server.ai;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.entities.PhysicalEntity;

public interface IArtificialIntelligence {

	void process(float tpf_secs);
	
	Vector3f getDirection(); // Current dir to point entity the right way
	
	void collided(PhysicalEntity pe);
}
