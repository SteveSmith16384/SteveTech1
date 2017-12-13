package com.scs.stetech1.systems;

import com.scs.stetech1.components.IEntity;

public abstract class AbstractSystem {

	public AbstractSystem() {
	}
	
	
	public abstract void process(IEntity entity, float tpf_secs);

}
