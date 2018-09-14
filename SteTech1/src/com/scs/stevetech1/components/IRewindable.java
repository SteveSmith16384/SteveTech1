package com.scs.stevetech1.components;

public interface IRewindable {

	void rewindPositionTo(long time);
	
	void restorePosition();
	
}
