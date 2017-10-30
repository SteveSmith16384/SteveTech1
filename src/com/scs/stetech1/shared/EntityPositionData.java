package com.scs.stetech1.shared;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public class EntityPositionData {

	public long serverTimestamp;	
	public Vector3f position;	
	public Quaternion rotation;

	public EntityPositionData() {
		super();
	}


	public EntityPositionData(Vector3f pos, Quaternion rot, long time) {
		this();

		this.position = pos;
		this.rotation = rot;
		this.serverTimestamp = time;
	}


	public EntityPositionData getInterpol(EntityPositionData other, long time) {
		//EntityPositionData newPD = new EntityPositionData();
		// interpolate between timestamps
		float frac = (this.serverTimestamp - time) / (time - other.serverTimestamp);
		Vector3f posToSet = this.position.interpolate(other.position, frac);

		Quaternion newRot = new Quaternion();
		Quaternion newRot2 = newRot;
		if (this.rotation != null) { // client-side EPD doesn't have any rot
			newRot2 = newRot.slerp(this.rotation, other.rotation, frac);
		}

		EntityPositionData epd = new EntityPositionData();
		epd.position = posToSet;
		epd.rotation = newRot2;
		epd.serverTimestamp = time;
		return epd;
	}

}
