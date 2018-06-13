package com.scs.stevetech1.netmessages;

import java.util.LinkedList;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.components.IEntity;

@Serializable
public class NewEntityMessage extends MyAbstractMessage {
	
	private static final int MAX_ENTITIES_PER_MSG = 10;

	public int gameId;
	public LinkedList<NewEntityData> data = new LinkedList<NewEntityData>(); // List of entities to create

	
	public NewEntityMessage() {
		super(true, true);
	}
	
	
	public NewEntityMessage(int _gameId) {
		super(true, false); // Not scheduled, since we need to know about the entity for when the position updates arrive
		
		gameId = _gameId;
	}
	
	
	public void add(IEntity e) {
		NewEntityData ned = new NewEntityData(e);
		data.add(ned);
		
		if (data.size() > MAX_ENTITIES_PER_MSG) {
			throw new RuntimeException("Maximum entities exceeded in message");
		}
	}


	public boolean isFull() {
		return this.data.size() >= MAX_ENTITIES_PER_MSG;
	}


}
