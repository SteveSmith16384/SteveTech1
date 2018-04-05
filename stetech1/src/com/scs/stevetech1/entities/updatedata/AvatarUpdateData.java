package com.scs.stevetech1.entities.updatedata;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.netmessages.EntityUpdateData;

@Serializable
public class AvatarUpdateData extends EntityUpdateData {

	public Vector3f aimDir;
	
	public AvatarUpdateData() {
		super();
	}
	
	public AvatarUpdateData(AbstractAvatar avatar, long timestamp) {
		super(avatar, timestamp);
		
		aimDir = avatar.getShootDir();
		//faceDir = avatar.getShootDir();
	}

}
