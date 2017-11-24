package com.scs.stetech1.jme;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;

public class MySimpleCharacterControl extends SimpleRigidBody { // todo - change package
	
	//private AbstractPlayersAvatar avatar;
	//private Vector3f walkDir = new Vector3f();

	public MySimpleCharacterControl(Spatial _entity, SimplePhysicsController _collChecker) {
		super(_entity, _collChecker, null);
	}


	public void setWalkDirection(Vector3f dir) {
		super.setLinearVelocity(dir);
	}
}
