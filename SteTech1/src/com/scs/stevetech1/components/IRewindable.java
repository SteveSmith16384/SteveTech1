package com.scs.stevetech1.components;

/**
 * Attach to entities that should be rewound when a player shoots.
 * @author stephencs
 *
 */
public interface IRewindable {

	void rewindPositionTo(long time);
	
	void restorePosition();
	
}
