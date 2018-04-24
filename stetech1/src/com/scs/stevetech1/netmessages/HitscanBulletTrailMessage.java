package com.scs.stevetech1.netmessages;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;

@Serializable
public class HitscanBulletTrailMessage extends MyAbstractMessage {

	public int playerID;
	public int shooterID;
	public int entityHitID; // If -1, no-one was hit, so use "end"
	public Vector3f end;
	
	public HitscanBulletTrailMessage() {
		// Serialize
	}
	
	
	public HitscanBulletTrailMessage(int _playerID, int _shooterID, int _entityHitID, Vector3f _end) {
		playerID =_playerID;
		shooterID = _shooterID;
		entityHitID = _entityHitID;
		end = _end;
	}
}
