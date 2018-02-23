package com.scs.stevetech1.netmessages;

import com.jme3.bounding.BoundingVolume;
import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.entities.PhysicalEntity;

/*
 * For debugging, to show server model bounds on the client
 */
@Serializable
public class ModelBoundsMessage extends MyAbstractMessage {

	public BoundingVolume bounds;
	
	public ModelBoundsMessage() {
		super(true, false);
	}
	

	public ModelBoundsMessage(PhysicalEntity pe) {
		this();
		
		bounds = pe.getMainNode().getWorldBound();
	}
	

}
