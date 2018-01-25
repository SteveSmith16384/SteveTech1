package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.entities.AbstractAvatar;

@Serializable
public class AvatarStatusMessage extends MyAbstractMessage {
	
	public int entityID;
	public boolean alive;
	
	public AvatarStatusMessage() {
		super(true);
	}


	public AvatarStatusMessage(AbstractAvatar avatar) {
		this();
		
		this.entityID = avatar.getID();
		alive = avatar.alive;
	}

}