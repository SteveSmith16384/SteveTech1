package com.scs.undercoveragent.entities;

import com.scs.stevetech1.avatartypes.PersonAvatar;
import com.scs.stevetech1.components.IDebrisTexture;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.UASimplePlayerData;
import com.scs.undercoveragent.UAStaticData;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;
import com.scs.undercoveragent.models.SnowmanModel;

public class SnowmanServerAvatar extends AbstractServerAvatar implements IDebrisTexture {

	public SnowmanServerAvatar(IEntityController _module, ClientData client, IInputDevice _input, int eid) {
		super(_module, UndercoverAgentClientEntityCreator.AVATAR, client, _input, eid, new SnowmanModel(_module.getAssetManager()), 2f, 0, new PersonAvatar(_module, _input, UAStaticData.MOVE_SPEED, UAStaticData.JUMP_FORCE));
	}


	@Override
	public void getReadyForGame() {
		super.getReadyForGame();

		UASimplePlayerData data = (UASimplePlayerData)this.client.playerData;
		data.score = 0;
		//this.sendStatusUpdateMessage(false);
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf) {
		super.processByServer(server, tpf);

		// Force player to jump if they haven't moved!
		if (this.alive) {// && server.gameData.isInGame()) {
			long timeSinceMove = System.currentTimeMillis() - super.avatarControl.getLastMoveTime();
			if (timeSinceMove > 7 * 1000) {
				//Globals.p("Forcing client to jump");
				this.avatarControl.jump();
			}
		}
	}


	@Override
	protected void setDied(IEntity killer, String reason) {
		super.setDied(killer, reason);

		if (killer != null && killer instanceof SnowmanServerAvatar) {
			SnowmanServerAvatar csp = (SnowmanServerAvatar)killer;
			csp.incScore(1);
		}
	}


	public void incScore(int i) {
		UASimplePlayerData data = (UASimplePlayerData)this.client.playerData;
		data.score += i;
		this.sendStatusUpdateMessage(false);
	}


	@Override
	public String getDebrisTexture() {
		return "Textures/snow.jpg";
	}


	@Override
	public float getMinDebrisSize() {
		return 0.001f;
	}


	@Override
	public float getMaxDebrisSize() {
		return 0.004f;
	}


	@Override
	public void updateClientSideHealth(int amt) {

	}


}
