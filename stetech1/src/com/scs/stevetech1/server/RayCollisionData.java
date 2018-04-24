package com.scs.stevetech1.server;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.entities.PhysicalEntity;

public class RayCollisionData implements Comparable<RayCollisionData> {

	public PhysicalEntity entity; // todo - rename
	public float distance;
	public Vector3f point;
	public long timestamp; // for debugging
	
	public RayCollisionData(PhysicalEntity _pe, Vector3f _point, float _dist) {
		entity = _pe;
		point = _point;
		distance = _dist;
	}


    public int compareTo(RayCollisionData other) {
        return Float.compare(distance, other.distance);
    }

    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof RayCollisionData){
            return ((RayCollisionData)obj).compareTo(this) == 0;
        }
        return super.equals(obj);
    }

    
    @Override
    public int hashCode() {
        return Float.floatToIntBits(distance);
    }


}
