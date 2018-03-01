package com.scs.stevetech1.systems.client;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;

@Serializable
public class LaunchData { // POJO

	public Vector3f startPos, dir;
	public int shooterId;
	public long launchTime;
	
	public LaunchData() {
		super(); // Need this for serialization
	}

	
	public LaunchData(Vector3f _startPos, Vector3f _dir, int _shooterId, long _launchTime) {
		super();
		
		startPos = _startPos;
		dir = _dir;
		shooterId = _shooterId;
		launchTime = _launchTime;
	}
	
}
