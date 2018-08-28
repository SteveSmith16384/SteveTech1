package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.systems.client.LaunchData;

/**
 * Another player has fired their weapon
 * @author stephencs
 *
 */
@Serializable
public class EntityLaunchedMessage extends MyAbstractMessage {

	public int entityID;
	public int playerID;
	public LaunchData launchData;

	public EntityLaunchedMessage() {
		super();
	}


	public EntityLaunchedMessage(int eid, int _playerID, LaunchData _launchData) {
		super(true, true);
		
		this.timestamp = _launchData.launchTime; // This will be earlier than most other messages, so the other clients launch immed
		
		entityID = eid;
		playerID = _playerID;
		launchData = _launchData;
	}
	
}
