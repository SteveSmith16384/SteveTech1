package com.scs.stevetech1.netmessages;

import java.util.HashMap;
import java.util.LinkedList;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.components.IEntity;

@Serializable
public class NewEntityMessage extends MyAbstractMessage {
	
	public int gameId;
	public LinkedList<NewEntityData> data = new LinkedList<NewEntityData>();


	public NewEntityMessage() {
		super(true, true);
	}
	
	
	public NewEntityMessage(int _gameId) {
		this();
		
		gameId = gameId;

	}
	
	
	public void Add(IEntity e) {
		//super(true, true);
		
		NewEntityData ned = new NewEntityData();
		ned.type = e.getType();
		ned.entityID = e.getID();
		ned.data = e.getCreationData();
		
		data.add(ned);
	}

}
