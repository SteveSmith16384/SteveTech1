package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class GameLogMessage extends MyAbstractMessage {

	//public LinkedList<String> log;
	public String logEntry;
	
	public GameLogMessage() {
		super(true, true);
	}


	public GameLogMessage(String _s) {
		this();
		
		//log = _log;
		logEntry = _s;
	}
}
