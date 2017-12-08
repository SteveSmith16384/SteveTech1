package com.scs.stetech1.components;


public interface ICausesHarmOnContact {//extends ICollideable {

	float getDamageCaused();
	
	int getSide(); // Prevent friendly-fire
	
	//ICanShoot getShooter();
	
	//void remove();
}
