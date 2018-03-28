package com.scs.stevetech1.netmessages;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.components.IEntity;

/**
 * This is used to inform the client that an entity has been killed.
 * If the entity killer or "killee" is an avatar, the client can inform the player.
 * The client should also remove the rigid body from the entity.
 * 
 */
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
