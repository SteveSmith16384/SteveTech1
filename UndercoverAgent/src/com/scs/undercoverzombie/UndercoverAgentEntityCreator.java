package com.scs.undercoverzombie;

import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.netmessages.NewEntityMessage;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.AbstractClientEntityCreator;

public class UndercoverAgentEntityCreator extends AbstractClientEntityCreator {

	public static final int HOUSE = 106;

	public UndercoverAgentEntityCreator() {

	}


	@Override
	public IEntity createEntity(NewEntityMessage msg) {
		/*if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
			Globals.p("Creating " + getName(msg.type));
		}*/
		int id = msg.entityID;

		switch (msg.type) {
		case HOUSE:
		{
			// todo
		}
		
		}
	}

}
