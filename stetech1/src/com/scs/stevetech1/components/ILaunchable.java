package com.scs.stevetech1.components;

import com.jme3.math.Vector3f;

public interface ILaunchable {

	IEntity getLauncher();
	
	void launch(IEntity _shooter, Vector3f startPos, Vector3f dir);
	
	//void launch(ICanShoot _shooter);
}
