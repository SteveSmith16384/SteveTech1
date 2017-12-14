package com.scs.stetech1.shared;

import com.scs.stetech1.components.IProcessByClient;
import com.scs.stetech1.components.IProcessByServer;
import com.scs.stetech1.entities.AbstractAvatar;
import com.scs.stetech1.entities.Entity;
import com.scs.stetech1.netmessages.AbilityUpdateMessage;
import com.scs.stetech1.server.AbstractGameServer;

public abstract class AbstractAbility extends Entity implements IAbility, IProcessByServer {//, IProcessByClient {

	private static final float SEND_INT = 5;

	//protected IEntityController game;
	protected AbstractAvatar owner;
	//protected String name;
	protected int num_;
	private float timeUntilNextSend = SEND_INT;
	//private int id;

	public AbstractAbility(IEntityController _game, int _id, int type, AbstractAvatar _owner, int _num, String _name) {
		super(_game, _id, type, _name);
		
		if (_owner == null) {
			throw new RuntimeException("No owner for ability");
		}
		
		//id = _id;
		//game = _game;
		num =_num;
		//name = _name;
		owner = _owner;
	}
	

	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		//if (game.isServer()) {
			timeUntilNextSend -= tpf_secs;
			if (timeUntilNextSend <= 0) {
				//AbstractGameServer server = (AbstractGameServer)game;
				server.networkServer.sendMessageToAll(new AbilityUpdateMessage(false, this.owner, num, this));
				timeUntilNextSend = SEND_INT;
			}
		//}
	}

}
