package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.systems.client.LaunchData;

@Serializable
public class EntityLaunchedMessage extends MyAbstractMessage {

	public int entityID;
	public LaunchData launchData;

	public EntityLaunchedMessage() {
		super();
	}


	public EntityLaunchedMessage(int eid, LaunchData _launchData) {
		this();
		
		entityID = eid;
		launchData = _launchData;
	}
	
}
