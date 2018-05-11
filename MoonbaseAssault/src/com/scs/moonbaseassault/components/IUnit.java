package com.scs.moonbaseassault.components;

import com.scs.stevetech1.entities.PhysicalEntity;

/**
 * Represents a moving soldier in the game, whether a player or AI soldier.
 *
 */
public interface IUnit { // todo - rename

	PhysicalEntity getPhysicalEntity();
	
	//AI getAI();
	
	//boolean canSee(PhysicalEntity pe);
	/*
	boolean isAlive();
	
	boolean hasAdequateHealth();
	
	int getSide();
	
	boolean hasRoute();
	
	int getRoutePriority();
	
	void setRoutePriority(int p);
	
	void setDest(int x, int y);
	*/
}
