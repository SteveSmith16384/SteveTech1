package com.scs.stevetech1.unittests.dummyentities;

import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.netmessages.NewEntityMessage;

public class DummyClient extends AbstractGameClient {

	public DummyClient() {
		super();
	}

	@Override
	protected IEntity actuallyCreateEntity(AbstractGameClient client, NewEntityMessage msg) {
		// TODO Auto-generated method stub
		return new DummyPhysicalEntity();
	}
}
