package com.scs.moonbaseassault.server.ai;

import com.scs.stevetech1.entities.PhysicalEntity;

public interface IArtificialIntelligence {

	void process(float tpf_secs);
	
	void collided(PhysicalEntity pe);
}
