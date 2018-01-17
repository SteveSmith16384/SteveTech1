package com.scs.stevetech1.netmessages;

import java.util.LinkedList;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.netmessages.EntityUpdateMessage.UpdateData;

@Serializable
public class EntityUpdateMessage extends MyAbstractMessage {
	
	public LinkedList<UpdateData> data = new LinkedList<UpdateData>();
	
	public EntityUpdateMessage() {
		super(false);
	}

	
	public void addEntityData(PhysicalEntity sc, boolean force) {
		UpdateData updateData = new UpdateData();
		updateData.entityID = sc.getID();
		updateData.pos = sc.getWorldTranslation();
		updateData.dir = sc.getWorldRotation();
		updateData.force = force;
		updateData.animation = sc.getCurrentAnim();
		this.data.add(updateData);

	}
	
	
	@Serializable
	public static class UpdateData {
		
		public int entityID;
		public Vector3f pos;
		public Quaternion dir;
		public boolean force; // Force new position on client, e.g. avatar restarting.
		public String animation;
		
	}
	
}
