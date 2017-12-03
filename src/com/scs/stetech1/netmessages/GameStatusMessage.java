package com.scs.stetech1.netmessages;

import java.util.ArrayList;

import com.jme3.network.serializing.Serializable;
import com.scs.stetech1.data.PlayerData;

@Serializable
public class GameStatusMessage extends MyAbstractMessage {

	public long gameTimeSecs;
	public int gameStatus;
	public ArrayList<PlayerData> players;
	
	// todo - fill these in
	public GameStatusMessage() {
		super(true);
	}

}
