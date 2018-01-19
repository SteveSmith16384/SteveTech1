package com.scs.stevetech1.shared;

import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.netmessages.NewEntityMessage;

public abstract class AbstractClientEntityCreator {

	public AbstractClientEntityCreator() {
	}


	public IEntity createEntity(AbstractGameClient game, NewEntityMessage msg) {
		int id = msg.entityID;

		switch (msg.type) {
		default:
			throw new RuntimeException("Unhandled entity type: " + msg.type);
		}
	}

}