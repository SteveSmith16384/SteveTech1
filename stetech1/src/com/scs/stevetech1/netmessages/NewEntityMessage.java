package com.scs.stevetech1.netmessages;

import java.util.LinkedList;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.components.IEntity;

@Serializable
public class NewEntityMessage extends MyAbstractMessage {
	
	private static final int MAX_ITEMS = 10;

	public int gameId;
	public LinkedList<NewEntityData> data = new LinkedList<NewEntityData>();


	public NewEntityMessage() {
		super(true, true);
	}
	
	
	public NewEntityMessage(int _gameId) {
		super(true, false); // Not scheduled, since we need to know about the entity for when the position updates arrive
		
		gameId = _gameId;

	}
	
	
	public void Add(IEntity e) {
		NewEntityData ned = new NewEntityData();
		ned.type = e.getType();
		ned.entityID = e.getID();
		ned.data = e.getCreationData();
		
		data.add(ned);
	}


	public boolean isFull() {
		return this.data.size() >= MAX_ITEMS;
	}


}
