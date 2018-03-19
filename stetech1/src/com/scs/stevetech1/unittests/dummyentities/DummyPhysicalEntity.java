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
	public void adjustWorldTranslation(Vector3f offset) {
		pos.addLocal(offset);
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public HashMap<String, Object> getCreationData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean requiresProcessing() {
		return false;
	}

}
