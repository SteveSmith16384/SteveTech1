package com.scs.unittestgame;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

public class DummyCamera extends Camera {
	
	public DummyCamera() {
		super(0, 0);
		
		this.setLocation(new Vector3f());
	}

}
