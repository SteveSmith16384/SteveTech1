package com.scs.stevetech1.netmessages;

import java.util.LinkedList;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.components.IClientSideAnimated;
import com.scs.stevetech1.entities.PhysicalEntity;

@Serializable
public class EntityUpdateMessage extends MyAbstractMessage {
	
	private static final int MAX_ITEMS = 10;
	
	public LinkedList<EntityUpdateData> data = new LinkedList<EntityUpdateData>();
	
	public EntityUpdateMessage() {
		super(false, false);
	}

	
	public void addEntityData(PhysicalEntity sc, boolean force, EntityUpdateData updateData) {
		//EntityUpdateData updateData = new UpdateData();
		/*updateData.entityID = sc.getID();
		updateData.pos = sc.getWorldTranslation();
		//updateData.dir = sc.getWorldRotation();
		updateData.force = force;
		if (sc instanceof IClientSideAnimated) {
			IClientSideAnimated csa = (IClientSideAnimated)sc;
			updateData.animationCode = csa.getCurrentAnimCode();
		}
		*/
		this.data.add(updateData);

	}
	
	
	public boolean isFull() {
		return this.data.size() >= MAX_ITEMS;
	}
	
/*	
	@Serializable
	public static class UpdateData {
		
		public int entityID;
		public Vector3f pos;
		public boolean force; // Force new position on client, e.g. avatar restarting.
		//public Quaternion dir;
		public int animationCode;
		
	}
	*/
}
