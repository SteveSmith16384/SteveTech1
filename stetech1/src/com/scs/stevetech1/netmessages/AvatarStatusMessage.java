package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.entities.AbstractAvatar;

@Serializable
public class AvatarStatusMessage extends MyAbstractMessage {
	
	public int entityID;
	public float health;
	
	public AvatarStatusMessage() {
		super();
	}


	public AvatarStatusMessage(AbstractAvatar avatar) {
		super(true, true);
		
		this.entityID = avatar.getID();
		this.health = avatar.getHealth();
	}

}