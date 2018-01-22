package com.scs.undercoveragent;

import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.netmessages.NewEntityMessage;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.AbstractClientEntityCreator;

public class UndercoverAgentClientEntityCreator extends AbstractClientEntityCreator {

	public static final int AVATAR = 1;
	public static final int FLOOR = 2;
	
	public static final int SNOWBALL_LAUNCHER = 10;
	public static final int SNOWBALL = 11;

	public UndercoverAgentClientEntityCreator() {
		super();
	}


	@Override
	public IEntity createEntity(AbstractGameClient game, NewEntityMessage msg) {
		/*if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
			Globals.p("Creating " + getName(msg.type));
		}*/
		int id = msg.entityID;

		switch (msg.type) {
		case SNOWBALL:
		{
			// todo
		}

		default:
			return super.createEntity(game, msg);
		}
	}
}

