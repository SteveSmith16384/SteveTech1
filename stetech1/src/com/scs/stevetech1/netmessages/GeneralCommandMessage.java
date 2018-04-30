package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class GeneralCommandMessage extends MyAbstractMessage {

	public int gameID;
	
	public enum Command {
		AllEntitiesSent, // todo - remove this
		RemoveAllEntities,
		GameRestarting, // So the client knows that all the entities are about to change
		GameRestarted,
	}
	
	public Command command;
	
	
	public GeneralCommandMessage() {
		super();
	}

	
	public GeneralCommandMessage(Command cmd, int _gameID) {
		super(true, false);
		
		command = cmd;
		gameID = _gameID;
	}


}