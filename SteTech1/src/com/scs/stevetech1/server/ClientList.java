package com.scs.stevetech1.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import com.scs.stevetech1.netmessages.WelcomeClientMessage;

public class ClientList {

	public HashMap<Integer, ClientData> clients = new HashMap<>(10); // PlayerID::ClientData
	private LinkedList<ClientData> clientsToAdd = new LinkedList<>();
	private LinkedList<Integer> clientsToRemove = new LinkedList<>();

	private AbstractGameServer server;

	public ClientList(AbstractGameServer _server) {
		super();

		server = _server;
	}


	public Collection<ClientData> getClients() {
		return clients.values();
	}


	public void addClient(ClientData data) {
		synchronized (clientsToAdd) {
			clientsToAdd.add(data);
		}
	}


	public void removeClient(int id) {
		synchronized(clientsToRemove) {
			this.clientsToRemove.add(id);
		}
	}


	public ClientData getClient(int id) {
		return this.clients.get(id);
	}


	public int getNumClients() {
		return clients.size();
	}
	

	public void addRemoveClients() {
		// Add/remove queued clients
		synchronized (clientsToAdd) {
			while (this.clientsToAdd.size() > 0) {
				ClientData client = this.clientsToAdd.remove();
				this.clients.put(client.id, client);
				server.gameNetworkServer.sendMessageToClient(client, new WelcomeClientMessage());
				//Globals.p("Actually added client " + client.id);
			}
		}

		synchronized (clientsToRemove) {
			while (this.clientsToRemove.size() > 0) {
				int id = this.clientsToRemove.remove();
				ClientData client = this.clients.remove(id);
				if (client != null) {
					server.playerLeft(client);
				}
				Globals.p("Actually removed client " + id);
			}
		}

	}



}
