package com.scs.undercoveragent;

import java.io.IOException;

import com.scs.stevetech1.lobby.AbstractLobbyServer;

public class UndercoverAgentLobbyServer extends AbstractLobbyServer {

	public static void main(String[] args) {
		try {
			new UndercoverAgentLobbyServer();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public UndercoverAgentLobbyServer() throws IOException {
		super(UndercoverAgentStaticData.LOBBY_PORT);
	}

}
