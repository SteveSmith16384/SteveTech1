package com.scs.stevetech1.components;

import com.jme3.math.Vector3f;

public interface ILaunchable { // todo - rename to PlayerLaunchable, also create a new IDontCollideWithOwner

	IEntity getLauncher(); // So we know who not to collide with
	
	void launch(IEntity _shooter, Vector3f startPos, Vector3f dir);
	
	boolean hasBeenLaunched();
	
}
