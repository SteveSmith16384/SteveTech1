package com.scs.stetech1.netmessages;

import java.util.ArrayList;

import com.jme3.network.serializing.Serializable;
import com.scs.stetech1.data.SimpleGameData;
import com.scs.stetech1.data.SimplePlayerData;

@Serializable
public class GameStatusMessage extends MyAbstractMessage {

	//public long gameTimeMS;
	//public int gameStatus;
	public SimpleGameData gameData;
	public ArrayList<SimplePlayerData> players;
	
	public GameStatusMessage() {
		super(true);
	}

	
	public GameStatusMessage(SimpleGameData _gameData, ArrayList<SimplePlayerData> _players) {
		super(true);
		
		//this.gameStatus = gameData.getStatus();
		//this.gameTimeMS = System.currentTimeMillis() - gameData.statusStartTime;
		this.gameData = _gameData;
		players = _players;
	}
	
	
}
