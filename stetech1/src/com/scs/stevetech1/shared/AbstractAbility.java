package com.scs.stevetech1.shared;

import java.util.HashMap;

import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.components.IProcessByServer;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.Entity;
import com.scs.stevetech1.netmessages.AbilityUpdateMessage;
import com.scs.stevetech1.server.AbstractGameServer;

public abstract class AbstractAbility extends Entity implements IAbility, IProcessByServer, IProcessByClient {

	private static final float SEND_INT_SECS = 5;

	protected AbstractAvatar owner;
	public int num;
	private float timeUntilNextSend_secs = SEND_INT_SECS;
	private long lastUpdateMsgTime;

	public AbstractAbility(IEntityController _game, int _id, int type, AbstractAvatar _owner, int _num, String _name) {
		super(_game, _id, type, _name);

		if (_owner == null) {
			throw new RuntimeException("No owner for ability");
		}

		owner = _owner;
		num = _num;

		if (game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("ownerid", owner.getID());
			creationData.put("num", num);
		}
		
		owner.ability[num] = this;
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		timeUntilNextSend_secs -= tpf_secs;
		if (timeUntilNextSend_secs <= 0) {
			server.networkServer.sendMessageToAll(new AbilityUpdateMessage(false, this));
			timeUntilNextSend_secs = SEND_INT_SECS;
		}
	}


	@Override
	public long getLastUpdateTime() {
		return this.lastUpdateMsgTime;
	}


	@Override
	public void setLastUpdateTime(long l) {
		this.lastUpdateMsgTime = l;
	}

}
