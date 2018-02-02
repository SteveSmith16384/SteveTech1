package com.scs.stevetech1.lobby;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.netmessages.lobby.UpdateLobbyMessage;

@Serializable
public class GameServerDetails {
	
	public UpdateLobbyMessage ulm;
	public long timeRcvd = System.currentTimeMillis();

	public GameServerDetails() {
		// For serialization
	}
	
	public GameServerDetails(UpdateLobbyMessage _ulm) {
		ulm = _ulm;
	}

}
