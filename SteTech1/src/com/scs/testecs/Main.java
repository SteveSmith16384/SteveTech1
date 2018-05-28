package com.scs.testecs;

import com.scs.stevetech1.server.Globals;
import com.scs.testecs.components.PositionData;
import com.scs.testecs.systems.ISystem;
import com.scs.testecs.systems.MovementSystem;

/*
 * Simple stand-alone project to test out an Entity Component System.
 * 
 */
public class Main {

	public Main() {
		TestEntity e = new TestEntity();
		e.components.put(PositionData.class, new PositionData());
		
		ISystem ms = new MovementSystem();
		PositionData mc = (PositionData)e.components.get(PositionData.class);
		Globals.p("Pos: " + mc.pos);
		ms.process(e);
		Globals.p("Pos: " + mc.pos);
	}

	public static void main(String[] args) {
		new Main();

	}

}
