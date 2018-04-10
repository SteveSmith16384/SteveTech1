package com.scs.moonbaseassault.server.ai;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.AbstractGameServer;

public interface IArtificialIntelligence {

	void process(AbstractEntityServer server, float tpf_secs);
	
	Vector3f getDirection(); // Current dir to point entity the right way
	
	PhysicalEntity getCurrentTarget();
	
	int getAnimCode();
	
	void collided(PhysicalEntity pe);
}
