package com.scs.stevetech1.netmessages;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;

/**
 * Sent from server to clients to indicate that Another player or AI has fired their weapon
 *
 */
/*
@Serializable
public class EntityLaunchedMessage extends MyAbstractMessage {

	public int playerID; // -1 if AI shooter
	public int bulletEntityID;
	public Vector3f startPos, dir;
	public int shooterId;
	public long launchTime;

	public EntityLaunchedMessage() {
		super();
	}


	public EntityLaunchedMessage(int _playerID, int _bulletEntityID, Vector3f _startPos, Vector3f _dir, int _shooterId, long _launchTime) {
		super(true, true);
		
		playerID = _playerID;
		bulletEntityID = _bulletEntityID;
		startPos = _startPos;
		dir = _dir;
		shooterId = _shooterId;
		launchTime = _launchTime;

		this.timestamp = _launchTime; // This will be earlier than most other messages, so the other clients launch immediately
		
}
	
}
*/