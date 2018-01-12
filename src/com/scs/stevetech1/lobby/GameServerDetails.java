package com.scs.stevetech1.lobby;

import com.scs.stevetech1.netmessages.lobby.UpdateLobbyMessage;

public class GameServerDetails {
	
	public UpdateLobbyMessage ulm;
	public long timeRcvd = System.currentTimeMillis();

	public GameServerDetails(UpdateLobbyMessage _ulm) {
		ulm = _ulm;
	}

}
