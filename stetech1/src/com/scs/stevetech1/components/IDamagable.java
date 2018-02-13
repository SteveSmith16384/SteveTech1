package com.scs.stevetech1.components;

public interface IDamagable {

	void damaged(float amt, ICausesHarmOnContact collider, String reason);
	
	int getSide(); // Prevent friendly-fire
	
}
