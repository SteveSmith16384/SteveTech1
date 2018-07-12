package com.scs.stevetech1.netmessages;

import java.util.LinkedList;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;

@Serializable
public class EntityUpdateMessage extends MyAbstractMessage {

	private static final int MAX_ITEMS = 12;

	public LinkedList<EntityUpdateData> data = new LinkedList<EntityUpdateData>();

	public EntityUpdateMessage() {
		super(false, false);
	}


	public void addEntityData(PhysicalEntity sc, boolean force, EntityUpdateData updateData) {
		this.data.add(updateData);
		
		if (updateData.animationCode == AbstractAvatar.ANIM_DIED) {
			//this.setReliable(true); // We must get the death anim to the server at all costs!
		}
	}


	public boolean isFull() {
		return this.data.size() >= MAX_ITEMS;
	}

}
