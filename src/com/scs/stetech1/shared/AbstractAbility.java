package com.scs.stetech1.shared;

import java.util.HashMap;

import com.scs.stetech1.components.IProcessByServer;
import com.scs.stetech1.entities.AbstractAvatar;
import com.scs.stetech1.entities.Entity;
import com.scs.stetech1.netmessages.AbilityUpdateMessage;
import com.scs.stetech1.server.AbstractGameServer;

public abstract class AbstractAbility extends Entity implements IAbility, IProcessByServer {//, IProcessByClient {

	private static final float SEND_INT = 5;

	protected AbstractAvatar owner;
	public int num;
	private float timeUntilNextSend = SEND_INT;
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
			creationData.put("ownerid", owner.id);
			creationData.put("num", num);
		}
		
		// Add to avatar
		if (num == 0) {
			owner.abilityGun = this;
		} else if (num == 1) {
			owner.abilityOther = this;
		} else {
			throw new RuntimeException("todo");
		}

	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		//if (game.isServer()) {
		timeUntilNextSend -= tpf_secs;
		if (timeUntilNextSend <= 0) {
			//AbstractGameServer server = (AbstractGameServer)game;
			server.networkServer.sendMessageToAll(new AbilityUpdateMessage(false, this));
			timeUntilNextSend = SEND_INT;
		}
		//}
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
