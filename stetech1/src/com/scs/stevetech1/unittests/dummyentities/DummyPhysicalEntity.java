package com.scs.stevetech1.unittests.dummyentities;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.components.IPhysicalEntity;

public class DummyPhysicalEntity implements IPhysicalEntity {
	
	public Vector3f pos = new Vector3f();

	public DummyPhysicalEntity() {

	}

	@Override
	public Vector3f getWorldTranslation() {
		return pos;
	}

	@Override
	public void setWorldTranslation(Vector3f newPos) {
		pos.set(newPos);
		
	}

	@Override
	public void adjustWorldTranslation(Vector3f offset) {
		pos.addLocal(offset);
		
	}


}
