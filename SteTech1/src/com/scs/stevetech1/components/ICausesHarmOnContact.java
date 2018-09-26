package com.scs.stevetech1.components;


/**
 * Anything implementing this interface will cause harm to an IDamagable on contact
 * @author stephencs
 *
 */
public interface ICausesHarmOnContact {

	float getDamageCaused();
	
	byte getSide(); // Prevent friendly-fire
	
	IEntity getActualShooter(); // We might be a bullet, but it's the avatar holding the gun that we want
	
}
