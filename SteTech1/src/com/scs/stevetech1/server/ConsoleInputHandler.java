package com.scs.stevetech1.server;

import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.netmessages.ModelBoundsMessage;

import ssmith.util.ConsoleInputListener;

public class ConsoleInputHandler implements ConsoleInputListener {
	
	private AbstractGameServer server;
	private String consoleInput;

	public ConsoleInputHandler(AbstractGameServer _server) {
		server = _server;
	}


	@Override
	public void processConsoleInput(String s) {
		//Globals.p("Received input: " + s);
		this.consoleInput = s;
	}


	public void checkConsoleInput() {
		try {
			if (this.consoleInput != null) {
				if (this.consoleInput.equalsIgnoreCase("help") || this.consoleInput.equalsIgnoreCase("?")) {
					Globals.p("mb, stats, entities");
				} else if (this.consoleInput.equalsIgnoreCase("mb")) {
					sendDebuggingBoxes();
				} else if (this.consoleInput.equalsIgnoreCase("stats")) {
					showStats();
				} else if (this.consoleInput.equalsIgnoreCase("entities") || this.consoleInput.equalsIgnoreCase("e")) {
					listEntities();
				} else {
					Globals.p("Unknown command: " + this.consoleInput);
				}
				this.consoleInput = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	private void sendDebuggingBoxes() {
		//synchronized (server.entities) {
			// Loop through the entities
			for (IEntity e : server.entities.values()) {
				if (e instanceof PhysicalEntity) {
					PhysicalEntity pe  = (PhysicalEntity)e;
					server.sendMessageToInGameClients(new ModelBoundsMessage(pe));
				}
			}
		//}
		Globals.p("Sent model bounds to all clients");
	}


	private void listEntities() {
		//synchronized (entities) {
		// Loop through the entities
		for (IEntity e : server.entities.values()) {
			Globals.p("Entity " + e.getID() + ": " + e.getName() + " (" + e + ")");
		}
		Globals.p("Total:" + server.getNumEntities());
		//}
	}


	private void showStats() {
		Globals.p("Game ID: " + server.getGameID());
		Globals.p("Game Status: " + SimpleGameData.getStatusDesc(server.gameData.getGameStatus()));
		Globals.p("Num Entities: " + server.entities.size());
		Globals.p("Num Entities for processing: " + server.entitiesForProcessing.size());
		Globals.p("Num Clients: " + server.clientList.getNumClients());
	}



}
