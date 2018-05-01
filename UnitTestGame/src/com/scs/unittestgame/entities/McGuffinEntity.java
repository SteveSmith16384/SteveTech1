package com.scs.unittestgame.entities;

import com.scs.stevetech1.entities.Entity;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.unittestgame.UnitTestGameServer;

public class McGuffinEntity extends Entity {

	public McGuffinEntity(IEntityController _module, int _id) {
		super(_module, _id, UnitTestGameServer.MCGUFFIN_ID, "McGffin", true);
	}
}
