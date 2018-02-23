package com.scs.stevetech1.netmessages;

import com.jme3.bounding.BoundingVolume;
import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.entities.PhysicalEntity;

@Serializable
public class SendModelBoundsMessage extends MyAbstractMessage {

	public BoundingVolume bounds;
	
	public SendModelBoundsMessage() {
		super(true, false);
	}
	

	public SendModelBoundsMessage(PhysicalEntity pe) {
		this();
		
		bounds = pe.getMainNode().getWorldBound();
	}
	

}
