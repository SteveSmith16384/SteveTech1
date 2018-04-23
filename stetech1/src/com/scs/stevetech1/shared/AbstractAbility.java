package com.scs.stevetech1.shared;

import java.util.HashMap;

import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.components.IProcessByServer;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.Entity;
import com.scs.stevetech1.netmessages.AbilityUpdateMessage;
import com.scs.stevetech1.server.AbstractEntityServer;

public abstract class AbstractAbility extends Entity implements IAbility, IProcessByServer, IProcessByClient {

	private static final float SEND_INT_SECS = 5;

	public int playerID;
	public int avatarID;
	protected AbstractAvatar owner;
	public int abilityNum;
	private float timeUntilNextSend_secs = SEND_INT_SECS;
	private long lastUpdateMsgTime;

	/**
	 * _owner is null on the client side.
	 * 
	 * @param _game
	 * @param _id
	 * @param type
	 * @param _playerID
	 * @param _owner
	 * @param _avatarID
	 * @param _abilityNum
	 * @param _name
	 */
	public AbstractAbility(IEntityController _game, int _id, int type, int _playerID, AbstractAvatar _owner, int _avatarID, int _abilityNum, String _name) {
		super(_game, _id, type, _name, true);

		/*if (_owner == null) {
			throw new RuntimeException("No owner for ability");
		}*/

		playerID = _playerID;
		owner = _owner;
		avatarID = _avatarID;
		abilityNum = _abilityNum;

		if (game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("ownerid", owner.getID());
			creationData.put("playerID", playerID);
			creationData.put("num", abilityNum);

			owner.ability[abilityNum] = this;
		}
		
	}


	@Override
	public void processByServer(AbstractEntityServer server, float tpf_secs) {
		timeUntilNextSend_secs -= tpf_secs;
		if (timeUntilNextSend_secs <= 0) {
			server.gameNetworkServer.sendMessageToAll(new AbilityUpdateMessage(false, this));
			timeUntilNextSend_secs = SEND_INT_SECS;
		}
	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		if (owner == null) {
			IEntity e = client.getEntity(this.getID());
			if (e != null) {
				owner = (AbstractAvatar)e;
			}
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
