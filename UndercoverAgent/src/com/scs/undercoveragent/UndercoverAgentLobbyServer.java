package com.scs.undercoveragent;

import java.io.IOException;

import com.scs.stevetech1.lobby.AbstractLobbyServer;

import ssmith.util.MyProperties;

public class UndercoverAgentLobbyServer extends AbstractLobbyServer {

	public static void main(String[] args) {
		try {
			MyProperties props = new MyProperties(args[0]);
			int lobbyPort = props.getPropertyAsInt("lobbyPort", 6144);
			int timeoutMillis = props.getPropertyAsInt("timeoutMillis", 100000);

			new UndercoverAgentLobbyServer(lobbyPort, timeoutMillis);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public UndercoverAgentLobbyServer(int lobbyPort, int timeout) throws IOException {
		super(lobbyPort, timeout);
	}

}
