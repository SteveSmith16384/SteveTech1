package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.components.IEntity;

@Serializable
public class EntityKilledMessage extends MyAbstractMessage {
	
	public int killedEntityID;
	public int killerEntityID;
	
	public EntityKilledMessage() {
		super();
	}


	public EntityKilledMessage(IEntity killed, IEntity killer) {
		super(true, true);
		
		this.killedEntityID = killed.getID();
		this.killerEntityID = killer != null ? killer.getID() : -1;
	}

}
