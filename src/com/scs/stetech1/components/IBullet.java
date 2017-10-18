package com.scs.stetech1.components;


public interface IBullet extends ICollideable {

	float getDamageCaused();
	
	ICanShoot getShooter();
	
	void remove();
}
