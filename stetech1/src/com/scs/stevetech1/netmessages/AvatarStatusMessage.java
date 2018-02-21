package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.server.ClientData;

@Serializable
public class AvatarStatusMessage extends MyAbstractMessage {
	
	public int entityID;
	public float health;
	public int score;
	public boolean damaged; // Signal to show HUD effect
	public float moveSpeed, jumpForce; // todo 
	
	public AvatarStatusMessage() {
		super();
	}


	public AvatarStatusMessage(AbstractServerAvatar avatar, ClientData client, boolean _damaged) {
		super(true, true);
		
		this.entityID = avatar.getID();
		this.health = avatar.getHealth();
		this.score = client.getScore();
		damaged = _damaged;
		this.moveSpeed = avatar.moveSpeed;
		this.jumpForce = avatar.getJumpForce();
	}

}