package com.scs.stetech1.netmessages;

public class NewPlayerMessage extends MyAbstractMessage {
	
	public String name;

	public NewPlayerMessage(String _name) {
		super(true);
		
		name = _name;
	}

}
