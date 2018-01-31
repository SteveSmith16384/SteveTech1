package com.scs.stevetech1.systems;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.components.ICanShoot;

@Serializable
public class LaunchData {

	public Vector3f dir;
	public ICanShoot shooter;
	public long launchTime;
	
	public LaunchData(Vector3f _dir, ICanShoot _shooter, long _launchTime) {
		super();
		
		dir = _dir;
		shooter = _shooter;
		launchTime = _launchTime;
	}
	
}
