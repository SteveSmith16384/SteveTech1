package com.scs.stevetech1.netmessages.lobby;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.netmessages.MyAbstractMessage;

/*
 * This is sent from a game server to the lobby server, to keep it informed of its status
 */
@Serializable
public class UpdateLobbyMessage extends MyAbstractMessage {

	public String name;
	public String gameServerIPAddress;
	public int port;
	public int totalPlayers;
	public boolean anySpaces; // Note that players can still join, but will be a spectator or something
	
	public UpdateLobbyMessage() {
		super();
	}


	public UpdateLobbyMessage(String _name, String _gameServerIPAddress, int _port, int _totalPlayers, boolean spaces) {
		super(true, false);
		
		name = _name;
		gameServerIPAddress = _gameServerIPAddress;
		port = _port;
		totalPlayers = _totalPlayers;
		this.anySpaces = spaces;
	}

}
