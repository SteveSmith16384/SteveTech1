package com.scs.stevetech1.components;

/**
 * An entity that has health, and can be damaged.
 * @author stephencs
 *
 */
public interface IDamagable {

	void damaged(float amt, IEntity collider, String reason);
	
	byte getSide(); // Prevent friendly-fire
	
	float getHealth();
	
	void updateClientSideHealth(int amt); // e.g. update HUD
	
}
