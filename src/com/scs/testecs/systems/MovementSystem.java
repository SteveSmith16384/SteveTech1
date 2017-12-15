package com.scs.testecs.systems;

import com.scs.testecs.TestEntity;
import com.scs.testecs.components.PositionData;

public class MovementSystem implements ISystem {

	public MovementSystem() {
	}

	@Override
	public void process(TestEntity e) {
		PositionData c = (PositionData)e.components.get(PositionData.class);
		c.pos.x++;
	}

}
