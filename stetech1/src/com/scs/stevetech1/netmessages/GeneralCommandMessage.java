package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class GeneralCommandMessage extends MyAbstractMessage {

	public int gameID;
	
	public enum Command {
		AllEntitiesSent, // Tells a client (who has joined halfway through a game) that they now have all the entities
		RemoveAllEntities,
		GameRestarting, // So the client knows that all the entities are about to change
		GameRestarted,
	}
	
	public Command command;
	
	
	public GeneralCommandMessage() {
		super();
	}

	
	public GeneralCommandMessage(Command cmd, int _gameID) {
		super(true, true); // Must be scheduled, otherwise we'll get the command to delete all entities and process it before we get the command to create the entities themselves.
		
		command = cmd;
		gameID = _gameID;
	}


}