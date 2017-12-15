package com.scs.testecs;

import java.util.HashMap;

import com.scs.testecs.components.IComponent;

public class TestEntity {
	
	public static int nextid = 0;
	public int id;
	public HashMap<Class, IComponent> components = new HashMap<Class, IComponent>();

	public TestEntity() {
		id = nextid++;
	}

}
