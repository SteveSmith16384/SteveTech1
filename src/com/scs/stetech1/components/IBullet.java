package com.scs.stetech1.components;


public interface IBullet {//extends ICollideable { // todo - rename to ICausesHarmOnContact

	float getDamageCaused();
	
	ICanShoot getShooter();
	
	void remove();
}
