package com.scs.stetech1.shared;

import com.jme3.math.Vector3f;
import com.scs.stetech1.shared.entities.PhysicalEntity;

public class HitData {

	public PhysicalEntity target;
	public Vector3f pos;
	
	public HitData(PhysicalEntity _target, Vector3f _pos) {
		super();
		
		this.target = _target;
		this.pos =_pos;
	}

}
