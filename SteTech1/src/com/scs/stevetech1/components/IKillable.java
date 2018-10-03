package com.scs.stevetech1.components;

import com.scs.stevetech1.entities.PhysicalEntity;

public interface IKillable {

	/**
	 * Do anything special on the client when the entity is "killed", e.g. remove from HUD.
	 * @param killer The entity that killed this entity.
	 */
	void handleKilledOnClientSide(PhysicalEntity killer);
	
}

