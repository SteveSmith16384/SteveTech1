package com.scs.stevetech1.systems.client;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.components.ICanShoot;

@Serializable
public class LaunchData {

	public Vector3f dir;
	public int shooterId;
	public long launchTime;
	
	public LaunchData() {
		super(); // Need this for serialization
	}

	
	public LaunchData(Vector3f _dir, int _shooterId, long _launchTime) {
		super();
		
		dir = _dir;
		shooterId = _shooterId;
		launchTime = _launchTime;
	}
	
}
