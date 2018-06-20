package com.scs.stevetech1.netmessages;

import java.util.LinkedList;

import com.jme3.network.serializing.Serializable;

@Serializable
public class GameLogMessage extends MyAbstractMessage {

	public LinkedList<String> log;
	
	public GameLogMessage() {
		super(true, true);
	}


	public GameLogMessage(LinkedList<String> _log) {
		this();
		
		log = _log;
	}
}
