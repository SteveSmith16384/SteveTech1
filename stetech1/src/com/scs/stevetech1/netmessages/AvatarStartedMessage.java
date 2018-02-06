package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.entities.AbstractAvatar;

@Serializable
public class AvatarStartedMessage extends MyAbstractMessage {
	
	public int avatarId;
	
	public AvatarStartedMessage() {
		super();
	}

	public AvatarStartedMessage(AbstractAvatar _avatarId) {
		super(true, true);
		
		avatarId = _avatarId.getID();
	}

}
