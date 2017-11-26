package com.scs.simplephysics;

import com.jme3.scene.Spatial;

public class SimpleCharacterControl<T> extends SimpleRigidBody<T> {

	public SimpleCharacterControl(Spatial s, SimplePhysicsController _controller, T _tag) {
		super(s, _controller, _tag);
	}
	

}
