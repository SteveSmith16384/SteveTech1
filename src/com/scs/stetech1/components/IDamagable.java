package com.scs.stetech1.components;

public interface IDamagable {

	void damaged(float amt, String reason);
	
	int getSide(); // Prevent friendly-fire
	
	//void destroyed();
	
}
