package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;

@Serializable
public class AnimationMessage extends MyAbstractMessage { // todo - delete
	
	public int entityID;
	public String animation;
	
	public AnimationMessage() {
		super(true);
	}


	public AnimationMessage(boolean reliable, int eid, String anim) {
		super(reliable);
		
		entityID = eid;
		animation = anim;
	}
	
	
}
