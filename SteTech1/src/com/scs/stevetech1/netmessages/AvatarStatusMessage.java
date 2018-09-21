package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.server.ClientData;

@Serializable
public class AvatarStatusMessage extends MyAbstractMessage {
	
	public int entityID;
	public short health;
	public boolean damaged; // Signal to show HUD effect
	public boolean collectedPickup; // to signal to HUD
	
	public AvatarStatusMessage() {
		super();
	}


	public AvatarStatusMessage(AbstractServerAvatar avatar, ClientData client, boolean _damaged, boolean _collectedPickup) {
		super(true, true);
		
		this.entityID = avatar.getID();
		this.health = (short)avatar.getHealth();
		damaged = _damaged;
		this.collectedPickup = _collectedPickup;
	
	}

}