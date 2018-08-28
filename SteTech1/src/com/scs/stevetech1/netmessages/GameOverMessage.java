package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

/**
 * 
 * @author stephencs
 *
 */
@Serializable
public class GameOverMessage extends MyAbstractMessage {
	
	public int winningSide;
	
	public GameOverMessage() {
		super(true, true);
	}


	public GameOverMessage(int _winningSide) {
		super(true, true);
		
		winningSide = _winningSide;
	}
	
}
