package com.scs.stevetech1.components;

import com.scs.stevetech1.entities.PhysicalEntity;

public interface IKillable {

	void handleKilledOnClientSide(PhysicalEntity killer);
	
}
