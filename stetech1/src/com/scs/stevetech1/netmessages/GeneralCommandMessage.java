package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class GeneralCommandMessage extends MyAbstractMessage {

	public enum Command {
		AllEntitiesSent,
		RequestGameData
	}
	
	public Command command;
	
	public GeneralCommandMessage() {
		super(true);
	}

	public GeneralCommandMessage(Command cmd) {
		super(true);
		
		command = cmd;
	}


}