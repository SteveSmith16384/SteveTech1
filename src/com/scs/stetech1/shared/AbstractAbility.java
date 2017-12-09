package com.scs.stetech1.shared;

import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.entities.AbstractAvatar;
import com.scs.stetech1.netmessages.AbilityUpdateMessage;
import com.scs.stetech1.server.AbstractGameServer;

public abstract class AbstractAbility implements IAbility {

	private static final float SEND_INT = 5;

	protected IEntityController game;
	protected AbstractAvatar owner;
	protected String name;
	protected int num;
	private float timeUntilNextSend = SEND_INT;

	public AbstractAbility(IEntityController _game, AbstractAvatar _owner, int _num, String _name) {
		game = _game;
		num =_num;
		name = _name;
		owner = _owner;
	}

	public void process(float tpf_secs) {
		if (game.isServer()) {
			timeUntilNextSend -= tpf_secs;
			if (timeUntilNextSend <= 0) {
				AbstractGameServer server = (AbstractGameServer)game;
				server.networkServer.sendMessageToAll(new AbilityUpdateMessage(false, this.owner, num));
				timeUntilNextSend = SEND_INT;
			}
		}
	}
}
