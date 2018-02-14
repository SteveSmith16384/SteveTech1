package com.scs.stevetech1.lobby;

import java.io.IOException;
import java.util.HashMap;

import com.esotericsoftware.kryonet.Connection;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.netmessages.lobby.ListOfGameServersMessage;
import com.scs.stevetech1.netmessages.lobby.RequestListOfGameServersMessage;
import com.scs.stevetech1.netmessages.lobby.UpdateLobbyMessage;
import com.scs.stevetech1.networking.IMessageServerListener;
import com.scs.stevetech1.server.Globals;

public abstract class AbstractLobbyServer implements IMessageServerListener {

	private HashMap<Integer, Connection> clients = new HashMap<Integer, Connection>(); // client id::data
	private HashMap<String, GameServerDetails> gameServers = new HashMap<String, GameServerDetails>(); // game name::data
	private KryonetLobbyServer lobbyServer;


	public AbstractLobbyServer(int port) throws IOException {
		lobbyServer = new KryonetLobbyServer(port, port, this, !Globals.LIVE_SERVER);
		// todo - loop through and remove game servers that we haven't heard of for a while
	}


	@Override
	public void connectionAdded(int id, Object net) {
		Globals.p("Client (player or server) connected to us");
		this.clients.put(id, (Connection)net);
	}


	@Override
	public void messageReceived(int clientid, MyAbstractMessage msg) {
		if (msg instanceof UpdateLobbyMessage) {
			UpdateLobbyMessage ulm = (UpdateLobbyMessage)msg;
			this.gameServers.remove(ulm.name);
			this.gameServers.put(ulm.name, new GameServerDetails(ulm));

			//Globals.p("Updated details for game server '" + ulm.name + "'");
		} else if (msg instanceof RequestListOfGameServersMessage) {
			//RequestListOfGameServersMessage rlogs = (RequestListOfGameServersMessage)msg;
			
			ListOfGameServersMessage logs = new ListOfGameServersMessage();
			for(GameServerDetails details : gameServers.values()) {
				logs.servers.add(details.ulm);
			}
			
			this.lobbyServer.sendMessageToClient(clients.get(clientid), logs);
		}

	}


	@Override
	public void connectionRemoved(int id) {
		Globals.p("Game server disconnected from us");

	}


}
