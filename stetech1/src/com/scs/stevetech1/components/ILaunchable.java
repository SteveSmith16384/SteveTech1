package com.scs.stevetech1.components;

public interface ILaunchable {

	ICanShoot getLauncher();
	
	void launch(ICanShoot _shooter);
}
