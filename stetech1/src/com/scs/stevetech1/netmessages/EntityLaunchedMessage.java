package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.systems.LaunchData;

@Serializable
public class EntityLaunchedMessage extends MyAbstractMessage {

	public int entityID;
	public LaunchData launchData;

	public EntityLaunchedMessage() {
		super(true);
	}


	public EntityLaunchedMessage(int eid, LaunchData _launchData) {
		this();
		
		entityID = eid;
		launchData = _launchData;
	}
	
}
