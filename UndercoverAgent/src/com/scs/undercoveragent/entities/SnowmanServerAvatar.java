package com.scs.undercoveragent.entities;

import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;
import com.scs.undercoveragent.models.SnowmanModel;

public class SnowmanServerAvatar extends AbstractServerAvatar {
	
	public SnowmanServerAvatar(IEntityController _module, ClientData client, IInputDevice _input, int eid) {
		super(_module, UndercoverAgentClientEntityCreator.AVATAR, client, _input, eid, new SnowmanModel(_module.getAssetManager()), 2f, 3f, 2f);
	}
	
	
	@Override
	public void processByServer(AbstractGameServer server, float tpf) {
		super.processByServer(server, tpf);
		
		// Force player to jump if they haven't moved!
		if (this.alive) {// && server.gameData.isInGame()) {
			long timeSinceMove = System.currentTimeMillis() - super.lastMoveTime;
			if (timeSinceMove > 5 * 1000) {
				//Globals.p("Forcing client to jump");
				this.jump();  //todo Send message forcing client to jump
			}
		}
	}


}
