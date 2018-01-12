package com.scs.stevetech1.netmessages;

import java.util.ArrayList;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.data.SimplePlayerData;

/**
 * This is for sending from the game server to the clients
 *
 */
@Serializable
public class GameStatusMessage extends MyAbstractMessage {

	public SimpleGameData gameData;
	public ArrayList<SimplePlayerData> players;
	
	public GameStatusMessage() {
		super(true);
	}

	
	public GameStatusMessage(SimpleGameData _gameData, ArrayList<SimplePlayerData> _players) {
		super(true);
		
		this.gameData = _gameData;
		players = _players;
	}
	
	
}
