package com.scs.stevetech1.netmessages;

import java.util.LinkedList;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.entities.PhysicalEntity;

@Serializable
public class EntityUpdateMessage extends MyAbstractMessage {

	private static final int MAX_ITEMS = 10;

	public LinkedList<EntityUpdateData> data = new LinkedList<EntityUpdateData>();

	public EntityUpdateMessage() {
		super(false, false);
	}


	public void addEntityData(PhysicalEntity sc, boolean force, EntityUpdateData updateData) {
		this.data.add(updateData);

	}


	public boolean isFull() {
		return this.data.size() >= MAX_ITEMS;
	}


}
