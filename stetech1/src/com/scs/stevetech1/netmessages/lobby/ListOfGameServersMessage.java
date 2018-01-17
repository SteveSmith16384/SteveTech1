package com.scs.stevetech1.netmessages.lobby;

import java.util.ArrayList;
import java.util.List;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.netmessages.MyAbstractMessage;

@Serializable
public class ListOfGameServersMessage extends MyAbstractMessage {
	
	public List<UpdateLobbyMessage> servers = new ArrayList<UpdateLobbyMessage>();

	public ListOfGameServersMessage() {
		super(true);
	}

}
