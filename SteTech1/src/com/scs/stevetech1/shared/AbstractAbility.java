package com.scs.stevetech1.shared;

import java.util.HashMap;

import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.components.IProcessByServer;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.Entity;
import com.scs.stevetech1.netmessages.AbilityUpdateMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;

public abstract class AbstractAbility extends Entity implements IAbility, IProcessByServer, IProcessByClient {

	private static final float SEND_INT_SECS = 5;

	public int playerID;
	public int avatarID;
	protected AbstractAvatar owner;
	public byte abilityNum;
	protected float timeUntilNextUpdateSend_secs = SEND_INT_SECS;
	private long lastUpdateMsgTime;
	private boolean goingToBeActivated = false;
	protected float shotInterval_secs;
	
	/**
	 * _owner is null on the client side.
	 */
	public AbstractAbility(IEntityController _game, int _id, int type, int _playerID, AbstractAvatar _owner, int _avatarID, byte _abilityNum, String _name, float shotInt) {
		super(_game, _id, type, _name, true);

		playerID = _playerID;
		owner = _owner;
		avatarID = _avatarID;
		abilityNum = _abilityNum;
		this.shotInterval_secs = shotInt;

		if (game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("ownerid", owner.getID());
			creationData.put("playerID", playerID);
			creationData.put("num", abilityNum);

			owner.ability[abilityNum] = this;
		}

	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		if (this.goingToBeActivated) {
			if (activate() == false) { // This will also send the message
				Globals.p("Warning - activate ability failed!");
			}
			this.goingToBeActivated = false;
		}
		timeUntilNextUpdateSend_secs -= tpf_secs;
		if (timeUntilNextUpdateSend_secs <= 0) {
			server.sendMessageToInGameClients(new AbilityUpdateMessage(false, this));
			timeUntilNextUpdateSend_secs = SEND_INT_SECS;
		}
	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		if (owner == null) {
			if (this.playerID == client.getPlayerID()) { // Otherwise we only have an EnemyAvatar to add the ability to
				IEntity e = client.getEntity(this.avatarID);
				if (e != null) {
					owner = (AbstractAvatar)e;
					owner.ability[abilityNum] = this;
				}
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


	@Override
	public void setToBeActivated(boolean b) {
		this.goingToBeActivated = b;
	}


	@Override
	public boolean isGoingToBeActivated() {
		return goingToBeActivated;
	}


}
