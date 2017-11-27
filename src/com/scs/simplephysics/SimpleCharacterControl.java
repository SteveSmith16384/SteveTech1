package com.scs.simplephysics;

import com.jme3.scene.Spatial;

/*
 * Need this for walkDir
 */
public class SimpleCharacterControl<T> extends SimpleRigidBody<T> {

	public SimpleCharacterControl(Spatial s, SimplePhysicsController _controller, T _tag) {
		super(s, _controller, _tag);
	}
	

}
