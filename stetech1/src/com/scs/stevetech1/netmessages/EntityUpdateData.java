package com.scs.stevetech1.netmessages;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.components.IAnimatedServerSide;
import com.scs.stevetech1.components.IGetRotation;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.shared.ITimeStamped;

@Serializable
public class EntityUpdateData implements ITimeStamped {

	public int entityID;
	public Vector3f pos;
	//public boolean force; // Force new position on client, e.g. avatar restarting.
	public long timestamp;
	public Vector3f aimDir;
	public int animationCode;

	public EntityUpdateData() {

	}


	public EntityUpdateData(PhysicalEntity pe, long _timestamp) {
		this();

		timestamp = _timestamp;
		entityID = pe.id;
		pos = pe.getWorldTranslation();
		
		if (pe instanceof IAnimatedServerSide) {
			IAnimatedServerSide csa = (IAnimatedServerSide)pe;
			this.animationCode = csa.getCurrentAnimCode();
		}
		if (pe instanceof IGetRotation) {
			IGetRotation ir = (IGetRotation)pe;
			this.aimDir = ir.getRotation();
		}
	}


	@Override
	public long getTimestamp() {
		return timestamp;
	}

}
