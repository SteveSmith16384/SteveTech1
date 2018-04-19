package com.scs.stevetech1.unittests.dummyentities;

import java.util.HashMap;

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
	public String getName() {
		return null;
	}

	@Override
	public int getID() {
		return 0;
	}

	@Override
	public int getType() {
		return 0;
	}

	@Override
	public HashMap<String, Object> getCreationData() {
		return null;
	}

	@Override
	public void remove() {
		
	}


	@Override
	public boolean requiresProcessing() {
		return false;
	}

	@Override
	public int getGameID() {
		return 0;
	}

}
